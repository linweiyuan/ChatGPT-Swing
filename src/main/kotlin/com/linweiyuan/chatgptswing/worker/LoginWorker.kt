package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.extensions.saveAccessToken
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.InetSocketAddress
import java.net.Proxy
import javax.swing.SwingWorker

class LoginWorker(private val mainFrame: MainFrame) : SwingWorker<Boolean, Void>() {

    override fun doInBackground(): Boolean {
        try {
            val connection = Jsoup.newSession().useDefault()

            // only login needs proxy to check whether the country is supported
            val proxyHost = mainFrame.proxyHostField.text.trim()
            val proxyPort = mainFrame.proxyPortField.text.trim()
            val actionCommand = mainFrame.buttonGroup.selection.actionCommand
            if (actionCommand != Constant.PROXY_TYPE_NONE) {
                if (proxyHost.isBlank() || proxyPort.isBlank()) {
                    "Please input proxy host and proxy port.".warn()
                    return false
                }

                when (mainFrame.buttonGroup.selection.actionCommand) {
                    Constant.PROXY_TYPE_HTTP -> {
                        connection.proxy(proxyHost, proxyPort.toInt())
                    }

                    Constant.PROXY_TYPE_SOCKS5 -> {
                        connection.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyHost, proxyPort.toInt())))
                    }
                }
            }

            val username = mainFrame.usernameField.text
            val password = String(mainFrame.passwordField.password)
            val response = connection.newRequest()
                .url(Constant.URL_LOGIN)
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("username" to username, "password" to password)))
                .execute()
            return response.saveAccessToken()
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false
        mainFrame.usernameField.isEditable = true
        mainFrame.passwordField.isEditable = true
        mainFrame.proxyHostField.isEditable = true
        mainFrame.proxyPortField.isEditable = true
        mainFrame.loginButton.isEnabled = true

        val ok = get()
        if (ok) {
            mainFrame.dispose()
            MainFrame(shouldLogin = false, firstTimeLogin = true)
        }
    }

}