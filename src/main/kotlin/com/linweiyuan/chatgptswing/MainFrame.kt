package com.linweiyuan.chatgptswing

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.extensions.wrapped
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.worker.LoginWorker
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.swing.*

class MainFrame(shouldLogin: Boolean) : JFrame(Constant.TITLE) {

    init {
        layout = GridBagLayout()

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
        val gridBagConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
        }

        val usernameField = JTextField(Constant.LOGIN_FIELD_WIDTH)
        add(usernameField.wrapped(Constant.USERNAME), gridBagConstraints.apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            weightx = 1.0
        })

        val passwordField = JPasswordField(Constant.LOGIN_FIELD_WIDTH)
        add(passwordField.wrapped(Constant.PASSWORD), gridBagConstraints.apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
        })

        val progressBar = JProgressBar().apply {
            isIndeterminate = true
            isVisible = false
        }
        add(progressBar, gridBagConstraints.apply {
            gridx = 0
            gridy = 2
            gridwidth = 2
        })

        val loginButton = JButton(Constant.LOGIN).apply {
            addActionListener {
                if (usernameField.text.isBlank() || String(passwordField.password).isBlank()) {
                    "Please input email and password first.".warn()
                    return@addActionListener
                }

                LoginWorker(progressBar, usernameField, passwordField, this, this@MainFrame).execute()
            }
        }
        add(loginButton, gridBagConstraints.apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
        })

        pack()
    }

    private fun initMainFrame() {
        val gridBagConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.BOTH
        }

        val contentField = JTextField()
        add(contentField.wrapped(Constant.CONTENT), gridBagConstraints.apply {
            gridx = 0
            gridy = 0
            weightx = 1.0
        })

        val chatPane = JTextPane().apply {
            isEditable = false
        }
        add(JScrollPane(chatPane).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }, gridBagConstraints.apply {
            gridx = 0
            gridy = 1
            weighty = 1.0
        })

        size = Dimension(888, 555)
    }
}

fun main() {
    if (System.getProperty("os.name") == "Linux") {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
    } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }

    val shouldLogin: Boolean

    val authSessionFile = File(System.getProperty("user.home"), Constant.AUTH_SESSION_FILE_NAME)
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