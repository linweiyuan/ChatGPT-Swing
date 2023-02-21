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
import javax.swing.*

class LoginWorker(
    private val progressBar: JProgressBar,
    private val usernameField: JTextField,
    private val passwordField: JPasswordField,
    private val loginButton: JButton,
    private val mainFrame: JFrame,
) : SwingWorker<AuthSession, Void>() {

    override fun doInBackground(): AuthSession? {
        updateUI()

        try {
            val connection = Jsoup.newSession()
                .useDefault()
                .proxy("127.0.0.1", 20171) // only login needs proxy

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

            response = connection.newRequest().url("https://explorer.api.openai.com/api/auth/session")
                .followRedirects(false)
                .execute()
            val responseBody = response.body()
            if (response.statusCode() != Constant.HTTP_OK || responseBody == "{}") {
                "Failed to get access token, please try again later.".warn()
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
        progressBar.isVisible = !progressBar.isVisible
        usernameField.isEditable = !usernameField.isEditable
        passwordField.isEditable = !passwordField.isEditable
        loginButton.isEnabled = !loginButton.isEnabled
    }

}