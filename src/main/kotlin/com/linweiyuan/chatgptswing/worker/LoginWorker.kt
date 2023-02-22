package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import javax.swing.*

class LoginWorker(
    private val progressBar: JProgressBar,
    private val usernameField: JTextField,
    private val passwordField: JPasswordField,
    private val proxyHostField: JTextField,
    private val proxyPortField: JTextField,
    private val buttonGroup: ButtonGroup,
    private val loginButton: JButton,
    private val mainFrame: JFrame,
) : SwingWorker<AuthSession, Void>() {

    override fun doInBackground(): AuthSession? {
        updateUI()

        try {
            val connection = Jsoup.newSession().useDefault()

            // only login needs proxy to check whether the country is supported
            val proxyHost = proxyHostField.text.trim()
            val proxyPort = proxyPortField.text.trim()
            val actionCommand = buttonGroup.selection.actionCommand
            if (actionCommand != Constant.PROXY_TYPE_NONE) {
                if (proxyHost.isBlank() || proxyPort.isBlank()) {
                    "Please input proxy host and proxy port.".warn()
                    return null
                }

                when (buttonGroup.selection.actionCommand) {
                    Constant.PROXY_TYPE_HTTP -> {
                        connection.proxy(proxyHost, proxyPort.toInt())
                    }

                    Constant.PROXY_TYPE_SOCKS5 -> {
                        connection.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyHost, proxyPort.toInt())))
                    }
                }
            }

            var response = connection.newRequest().url("https://explorer.api.openai.com/api/auth/csrf").execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                "Failed to get CSRF token. ${response.statusMessage()}".warn()
                return null
            }

            val csrfToken = JSON.parseObject(response.body()).getString("csrfToken")
            response = connection.newRequest().url("https://explorer.api.openai.com/api/auth/signin/auth0?")
                .data("callbackUrl", "/")
                .data("csrfToken", csrfToken)
                .data("json", "true") // need to test with false
                .method(Connection.Method.POST)
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                "Failed to get authorized url. ${response.statusMessage()}".warn()
                return null
            }

            val authorizeUrl = JSON.parseObject(response.body()).getString("url")
            response = connection.newRequest().url(authorizeUrl).execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                "Failed to get state. ${response.statusMessage()}".warn()
                return null
            }

            val username = usernameField.text
            val href = response.parse().select("a").attr("href")
            val state = href.substring(href.indexOf('=') + 1)
            response = connection.newRequest().url("https://auth0.openai.com/u/login/identifier?state=$state")
                .data("username", username)
                .method(Connection.Method.POST)
                .execute()
            if (response.statusCode() == Constant.HTTP_BAD_REQUEST) {
                response.parse().select("#error-element-username").text().warn()
                return null
            }

            val password = String(passwordField.password)
            response = connection.newRequest().url("https://auth0.openai.com/u/login/password?state=$state")
                .data("username", username)
                .data("password", password)
                .method(Connection.Method.POST)
                .execute()
            if (response.statusCode() == Constant.HTTP_BAD_REQUEST) {
                "Email or password is not correct.".warn()
                return null
            }

            response = connection.newRequest().url("https://explorer.api.openai.com/api/auth/session").execute()
            val responseBody = response.body()
            if (response.statusCode() != Constant.HTTP_OK) {
                "Failed to get access token, please try again later.".warn()
                return null
            }

            if (responseBody == "{}") {
                "OpenAI's services are not available in your country.".warn()
                return null
            }

            return JSON.parseObject(response.body(), AuthSession::class.java)
        } catch (e: Exception) {
            e.toString().warn()
            return null
        }
    }

    override fun done() {
        updateUI()

        val authSession = get()
        if (authSession != null) {
            File(System.getProperty("user.home"), Constant.AUTH_SESSION_FILE_NAME).writeText(
                JSON.toJSONString(authSession, JSONWriter.Feature.PrettyFormat)
            )
            mainFrame.dispose()
            MainFrame(false)
        }
    }

    private fun updateUI() {
        progressBar.isIndeterminate = !progressBar.isIndeterminate
        usernameField.isEditable = !usernameField.isEditable
        passwordField.isEditable = !passwordField.isEditable
        proxyHostField.isEditable = !proxyHostField.isEditable
        proxyPortField.isEditable = !proxyPortField.isEditable
        loginButton.isEnabled = !loginButton.isEnabled
    }

}