package com.linweiyuan.chatgptswing

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.extensions.wrapped
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
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

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

        val conversationTreeModel = DefaultTreeModel(DefaultMutableTreeNode("ROOT"))
        val conversationTree = JTree(conversationTreeModel).apply {
            isRootVisible = false
            expandsSelectedPaths = true
            addTreeSelectionListener {
                if (selectionPaths == null) {
                    return@addTreeSelectionListener
                }

                val currentNode = it.path.lastPathComponent as DefaultMutableTreeNode
                val rootNode = conversationTreeModel.root
                // the message node
                if (currentNode.parent != null && currentNode.parent != rootNode) {
                    val message = currentNode.userObject as Message
                    chatPane.text = CacheUtil.getMessage(message.id)
                } else {
                    // the conversation node
                    val conversation = currentNode.userObject as Conversation
                    val conversationId = conversation.id

                    IdUtil.setConversationId(conversationId)

                    val text = CacheUtil.getConversation(conversationId)
                    if (text.isNullOrBlank()) {
                        GetConversationContentWorker(
                            authSession.accessToken,
                            conversation,
                            progressBar,
                            chatPane,
                            this,
                        ).execute()
                    } else {
                        chatPane.contentType = Constant.TEXT_HTML
                        chatPane.text = text
                    }
                    chatPane.text = text
                }
            }
        }

        conversationTree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val path = conversationTree.getPathForLocation(e.x, e.y) ?: return

                conversationTree.selectionPath = path

                val currentNode = path.lastPathComponent as DefaultMutableTreeNode
                // message
                if (currentNode.parent != conversationTree.model.root) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val message = currentNode.userObject as Message
                        showConversationMessagePopupMenu(e, message)
                    }
                } else {
                    // conversation
                    val conversation = currentNode.userObject as Conversation
                    if (SwingUtilities.isMiddleMouseButton(e)) {
                        GetConversationContentWorker(
                            authSession.accessToken,
                            conversation,
                            progressBar,
                            chatPane,
                            conversationTree,
                        ).execute()
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        showConversationPopupMenu(e, conversation)
                    }
                }
            }

            private fun showConversationPopupMenu(e: MouseEvent, conversation: Conversation) {
                val conversationTreePopupMenu = JPopupMenu().apply {
                    add(JMenuItem(Constant.REFRESH).apply {
                        addActionListener {
                            GetConversationContentWorker(
                                authSession.accessToken,
                                conversation,
                                progressBar,
                                chatPane,
                                conversationTree,
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
                                conversationTree,
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
                            DeleteConversationWorker(
                                authSession.accessToken,
                                conversationId,
                                progressBar,
                                conversationTree,
                            ).execute()
                        }
                    })
                }
                conversationTreePopupMenu.show(e.component, e.x, e.y)
            }

            private fun showConversationMessagePopupMenu(e: MouseEvent, message: Message) {
                JPopupMenu().apply {
                    add(JMenuItem(Constant.FEEDBACK).apply {
                        addActionListener {
                            val option = JOptionPane.showOptionDialog(
                                null,
                                "Choose a feedback.",
                                Constant.FEEDBACK,
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                arrayOf(Constant.FEEDBACK_LIKE, Constant.FEEDBACK_DISLIKE, Constant.FEEDBACK_CANCEL),
                                null,
                            )
                            if (option == JOptionPane.CANCEL_OPTION) {
                                return@addActionListener
                            }

                            val rating = if (option == JOptionPane.YES_OPTION) {
                                Constant.FEEDBACK_THUMBS_UP
                            } else {
                                Constant.FEEDBACK_THUMBS_DOWN
                            }
                            FeedbackConversationWorker(authSession.accessToken, progressBar, message, rating).execute()
                        }
                    })
                }.show(e.component, e.x, e.y)
            }
        })

        val leftPanel = JPanel().apply {
            layout = BorderLayout()

            add(JScrollPane(conversationTree).apply {
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            })

            add(JPanel().apply {
                layout = GridLayout(3, 1)

                add(JButton(Constant.NEW).apply {
                    addActionListener {
                        chatPane.border = null
                        chatPane.text = ""
                        IdUtil.clearIds()
                        conversationTree.clearSelection()
                    }
                })
                add(JButton(Constant.REFRESH).apply {
                    addActionListener {
                        GetConversationListWorker(
                            authSession.accessToken,
                            progressBar,
                            conversationTree,
                        ).execute()
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
                            conversationTree,
                        ).execute()
                    }
                })
            }, BorderLayout.SOUTH)
        }

        val contentField = JTextField().apply {
            addActionListener {
                ChatWorker(
                    authSession.accessToken,
                    progressBar,
                    this,
                    chatPane,
                    conversationTree
                ).execute()
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
        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            dividerLocation = Constant.SPLIT_PANE_DIVIDER_LOCATION
        })

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

        size = Dimension(Constant.DEFAULT_WIDTH, Constant.DEFAULT_HEIGHT)

        GetConversationListWorker(authSession.accessToken, progressBar, conversationTree).execute()

        if (firstTimeLogin) {
            val username = System.getProperty("user.name")
            if (Locale.getDefault().language == "zh") {
                contentField.text = String.format(Constant.GREETING_CHINESE, username)
            } else {
                contentField.text = String.format(Constant.GREETING_ENGLITH, username)
            }
            ChatWorker(
                authSession.accessToken,
                progressBar,
                contentField,
                chatPane,
                conversationTree
            ).execute()
        }

        if (!firstTimeLogin) {
            val expirationDate = ZonedDateTime.parse(authSession.expires)
            val currentDate = ZonedDateTime.now(expirationDate.zone)
            if (currentDate.isAfter(expirationDate.minusDays(Constant.TOKEN_RENEW_BEFORE_EXPIRATION_DAYS))) {
                RenewAccessTokenWorker(authSession).execute()
            }
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