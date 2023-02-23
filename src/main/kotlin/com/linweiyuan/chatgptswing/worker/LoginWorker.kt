package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
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
) : SwingWorker<Boolean, Void>() {

    override fun doInBackground(): Boolean {
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
                    return false
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

            val username = usernameField.text
            val password = String(passwordField.password)
            val response = connection.newRequest()
                .url(Constant.URL_LOGIN)
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("username" to username, "password" to password)))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            val authSession = JSON.parseObject(response.body(), AuthSession::class.java)
            File(System.getProperty("user.home"), Constant.AUTH_SESSION_FILE_NAME).writeText(
                JSON.toJSONString(authSession, JSONWriter.Feature.PrettyFormat)
            )
            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun done() {
        updateUI()

        val ok = get()
        if (ok) {
            mainFrame.dispose()
            MainFrame(shouldLogin = false, firstTimeLogin = true)
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