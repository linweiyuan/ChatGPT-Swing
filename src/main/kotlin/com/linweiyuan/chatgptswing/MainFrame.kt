package com.linweiyuan.chatgptswing

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.extensions.wrapped
import com.linweiyuan.chatgptswing.misc.Constant
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.swing.*

class MainFrame(shouldLogin: Boolean) : JFrame(Constant.TITLE) {

    init {
        if (shouldLogin) {
            initLoginFrame()
        } else {
            initMainFrame()
        }

        setLocationRelativeTo(null)
        isResizable = false
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }

    private fun initLoginFrame() {
        layout = GridBagLayout()

        val gridBagConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
        }

        val usernameField = JTextField(30)
        add(usernameField.wrapped(Constant.USERNAME), gridBagConstraints.apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            weightx = 1.0
        })

        val passwordField = JPasswordField(20)
        add(passwordField.wrapped(Constant.PASSWORD), gridBagConstraints.apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
        })

        val loginButton = JButton(Constant.LOGIN)
        add(loginButton, gridBagConstraints.apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
        })

        pack()
    }

    private fun initMainFrame() {
        pack()
    }
}

fun main() {
    if (System.getProperty("os.name") == "Linux") {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
    } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }

    val shouldLogin: Boolean

    val userHome = System.getProperty("user.home")
    val authSessionFile = File(userHome, Constant.AUTH_SESSION_FILE_NAME)
    shouldLogin = if (authSessionFile.exists()) {
        val authSessionJson = authSessionFile.readText()
        if (authSessionJson.isBlank()) {
            true
        } else {
            val authSession = JSON.parseObject(authSessionJson, AuthSession::class.java)

            val zoneId = ZoneId.systemDefault()
            val expireTime = ZonedDateTime.parse(authSession.expires).withZoneSameInstant(zoneId)
            val currentTime = ZonedDateTime.now(zoneId)
            authSession.accessToken.isBlank() || expireTime.isBefore(currentTime)
        }
    } else {
        true
    }

    SwingUtilities.invokeLater {
        MainFrame(shouldLogin)
    }
}