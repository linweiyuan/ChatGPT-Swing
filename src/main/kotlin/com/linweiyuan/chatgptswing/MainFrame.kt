package com.linweiyuan.chatgptswing

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.extensions.wrapped
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import com.linweiyuan.chatgptswing.worker.*
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URI
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.swing.*

class MainFrame(shouldLogin: Boolean, firstTimeLogin: Boolean = false) : JFrame(Constant.TITLE) {

    init {
        if (shouldLogin) {
            initLoginFrame()
        } else {
            initMainFrame(firstTimeLogin)
        }

        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }

    private fun initLoginFrame() {
        layout = GridBagLayout()

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

        val proxyHostField = JTextField(Constant.LOGIN_FIELD_WIDTH)
        add(proxyHostField.wrapped(Constant.PROXY_HOST), gridBagConstraints.apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
        })

        val proxyPortField = JTextField(Constant.LOGIN_FIELD_WIDTH)
        add(proxyPortField.wrapped(Constant.PROXY_PORT), gridBagConstraints.apply {
            gridx = 1
            gridy = 1
            gridwidth = 1
        })

        val buttonGroup = ButtonGroup()
        val proxyButtonPanel = JPanel().apply {
            val noneProxyButton = JRadioButton(Constant.PROXY_TYPE_NONE).apply {
                isSelected = true
                actionCommand = Constant.PROXY_TYPE_NONE
            }
            val httpProxyButton = JRadioButton(Constant.PROXY_TYPE_HTTP).apply {
                actionCommand = Constant.PROXY_TYPE_HTTP
            }
            val socks5ProxyButton = JRadioButton(Constant.PROXY_TYPE_SOCKS5).apply {
                actionCommand = Constant.PROXY_TYPE_SOCKS5
            }

            buttonGroup.add(noneProxyButton)
            buttonGroup.add(httpProxyButton)
            buttonGroup.add(socks5ProxyButton)

            add(noneProxyButton)
            add(httpProxyButton)
            add(socks5ProxyButton)
        }

        add(proxyButtonPanel, gridBagConstraints.apply {
            gridx = 0
            gridy = 2
            gridwidth = 2
        })

        val progressBar = JProgressBar()
        add(progressBar, gridBagConstraints.apply {
            gridx = 0
            gridy = 4
            gridwidth = 2
        })

        val loginButton = JButton(Constant.LOGIN).apply {
            addActionListener {
                if (usernameField.text.isBlank() || String(passwordField.password).isBlank()) {
                    "Please input email and password first.".warn()
                    return@addActionListener
                }
                LoginWorker(
                    progressBar,
                    usernameField,
                    passwordField,
                    proxyHostField,
                    proxyPortField,
                    buttonGroup,
                    this,
                    this@MainFrame
                ).execute()
            }
        }
        add(loginButton, gridBagConstraints.apply {
            gridx = 0
            gridy = 3
            gridwidth = 2
        })

        pack()
    }

    private fun initMainFrame(firstTimeLogin: Boolean) {
        layout = BorderLayout()

        val json = File(System.getProperty("user.home"), Constant.AUTH_SESSION_FILE_NAME).readText()
        val authSession = JSON.parseObject(json, AuthSession::class.java)

        val progressBar = JProgressBar()

        val chatPane = JTextPane().apply {
            isEditable = false
        }

        val conversations = mutableListOf<Conversation>()
        val conversationListModel = ConversationListModel(conversations)
        conversationListModel.clear() // make sure the default new chat is displayed
        val conversationList = JList(conversationListModel).apply {
            selectedIndex = 0 // default to start new chat

            addListSelectionListener {
                if (!it.valueIsAdjusting) {
                    val conversationId = conversations[this.selectedIndex].id
                    if (conversationId.isBlank()) {
                        chatPane.border = null
                        chatPane.text = ""
                        IdUtil.clearIds()
                        return@addListSelectionListener
                    }

                    IdUtil.setConversationId(conversationId)

                    val text = CacheUtil.getConversation(conversationId)
                    if (text.isNullOrBlank()) {
                        GetConversationContentWorker(
                            authSession.accessToken,
                            conversations[selectedIndex].id,
                            progressBar,
                            chatPane,
                        ).execute()
                    } else {
                        chatPane.contentType = Constant.TEXT_HTML
                        chatPane.text = text
                    }
                }
            }
        }

        conversationList.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val selectedIndex = conversationList.locationToIndex(e.point)
                    if (selectedIndex == 0) {
                        return
                    }

                    conversationList.selectedIndex = selectedIndex
                    showPopupMenu(e, selectedIndex)
                }
            }

            private fun showPopupMenu(e: MouseEvent, selectedIndex: Int) {
                val conversationListPopupMenu = JPopupMenu().apply {
                    add(JMenuItem(Constant.REFRESH).apply {
                        addActionListener {
                            GetConversationContentWorker(
                                authSession.accessToken,
                                conversations[selectedIndex].id,
                                progressBar,
                                chatPane,
                            ).execute()
                        }
                    })

                    add(JMenuItem(Constant.RENAME).apply {
                        addActionListener {
                            val title = JOptionPane.showInputDialog("Rename to new title.")
                            if (title.isNullOrBlank()) {
                                "Please input new title.".warn()
                                return@addActionListener
                            }

                            val conversationId = IdUtil.getConversationId()
                            if (conversationId.isBlank()) {
                                "This conversation does not support rename.".warn()
                                return@addActionListener
                            }

                            RenameConversationTitleWorker(
                                authSession.accessToken,
                                conversationId,
                                title,
                                progressBar,
                                conversationList,
                            ).execute()
                        }
                    })

                    add(JMenuItem(Constant.DELETE).apply {
                        addActionListener {
                            val option = JOptionPane.showConfirmDialog(null, "Do you want to delete this conversion?")
                            if (option != JOptionPane.OK_OPTION) {
                                return@addActionListener
                            }

                            val conversationId = IdUtil.getConversationId()
                            if (conversationId.isBlank()) {
                                "This conversation can not be deleted.".warn()
                                return@addActionListener
                            }

                            DeleteConversationWorker(
                                authSession.accessToken,
                                conversationId,
                                progressBar,
                                conversationList,
                            ).execute()
                        }
                    })
                }
                conversationListPopupMenu.show(e.component, e.x, e.y)
            }
        })

        val leftPanel = JPanel().apply {
            layout = BorderLayout()

            add(JScrollPane(conversationList).apply {
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            })

            add(JPanel().apply {
                layout = GridLayout(2, 1)

                add(JButton(Constant.REFRESH).apply {
                    addActionListener {
                        GetConversationListWorker(authSession.accessToken, progressBar, conversationList).execute()
                    }
                })
                add(JButton(Constant.CLEAR).apply {
                    addActionListener {
                        val option = JOptionPane.showConfirmDialog(null, "Do you want to clear all conversions?")
                        if (option != JOptionPane.OK_OPTION) {
                            return@addActionListener
                        }

                        ClearAllConversationsWorker(
                            authSession.accessToken,
                            progressBar,
                            conversationList,
                        ).execute()
                    }
                })
            }, BorderLayout.SOUTH)
        }

        val contentField = JTextField().apply {
            addActionListener {
                ChatWorker(authSession.accessToken, progressBar, this, chatPane, conversationList).execute()
            }
        }
        val rightPanel = JPanel().apply {
            layout = GridBagLayout()

            val gridBagConstraints = GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
            }

            add(contentField.wrapped(Constant.CONTENT), gridBagConstraints.apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                weighty = -1.0
            })

            add(JScrollPane(chatPane).apply {
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            }, gridBagConstraints.apply {
                gridx = 0
                gridy = 1
                weighty = 1.0
            })

            val ttsButton = JButton(Constant.TTS).apply {
                addActionListener {
                    val text = chatPane.selectedText
                    if (text.isBlank()) {
                        "Please select some texts first.".warn()
                        return@addActionListener
                    }

                    TTSWorker(progressBar, text, this).execute()
                }
            }
            add(ttsButton, gridBagConstraints.apply {
                gridx = 0
                gridy = 2
                weighty = -1.0
            })
        }

        add(progressBar, BorderLayout.NORTH)
        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel))

        val aboutMenuItem = JMenuItem(Constant.ABOUT).apply {
            addActionListener {
                JOptionPane.showMessageDialog(
                    null,
                    JLabel(Constant.ABOUT_INTO).apply {
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseClicked(e: MouseEvent) {
                                if (e.button == MouseEvent.BUTTON1) {
                                    Desktop.getDesktop().browse(URI(Constant.GITHUB_REPO_URL))
                                }
                            }
                        })
                    })
            }
        }
        val moreMenu = JMenu(Constant.MORE).apply {
            add(aboutMenuItem)
        }
        val menuBar = JMenuBar().apply {
            add(moreMenu)
        }
        jMenuBar = menuBar

        size = Dimension(1366, 768)

        GetConversationListWorker(authSession.accessToken, progressBar, conversationList).execute()

        if (firstTimeLogin) {
            val username = System.getProperty("user.name")
            if (Locale.getDefault().language == "zh") {
                contentField.text = String.format(Constant.GREETING_CHINESE, username)
            } else {
                contentField.text = String.format(Constant.GREETING_ENGLITH, username)
            }
            ChatWorker(authSession.accessToken, progressBar, contentField, chatPane, conversationList).execute()
        }
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