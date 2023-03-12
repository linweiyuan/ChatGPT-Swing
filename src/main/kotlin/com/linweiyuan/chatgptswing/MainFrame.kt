package com.linweiyuan.chatgptswing

import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.extensions.wrapped
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.ConfigUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import com.linweiyuan.chatgptswing.worker.*
import org.fife.ui.rsyntaxtextarea.FileTypeUtil
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class MainFrame : JFrame(Constant.TITLE) {
    val progressBar: JProgressBar = JProgressBar()
    lateinit var conversationTree: JTree
    lateinit var contentField: JTextField
    lateinit var textArea: RSyntaxTextArea
    lateinit var ttsButton: JButton

    init {
        initMainFrame()
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }

    private fun initMainFrame() {
        layout = BorderLayout()

        initConversationTree()
        val leftPanel = initLeftPanel()

        contentField = initContentField()
        textArea = initTextArea()
        val rightPanel = initRightPanel()

        jMenuBar = initMenuBar()

        add(progressBar, BorderLayout.NORTH)
        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            dividerLocation = Constant.SPLIT_PANE_DIVIDER_LOCATION
        })
        size = Dimension(Constant.DEFAULT_WIDTH, Constant.DEFAULT_HEIGHT)

        if (ConfigUtil.getServerUrl().isNotBlank() && ConfigUtil.getAccessToken().isNotBlank()) {
            progressBar.isIndeterminate = true
            SwingUtilities.invokeLater {
                GetConversationListWorker(this).execute()
            }
        }
    }

    private fun initConversationTree() {
        val conversationTreeModel = DefaultTreeModel(DefaultMutableTreeNode("ROOT"))
        conversationTree = JTree(conversationTreeModel).apply {
            isRootVisible = false
            expandsSelectedPaths = true
            addTreeSelectionListener {
                if (selectionPaths == null) {
                    return@addTreeSelectionListener
                }

                textArea.border = null

                val currentNode = it.path.lastPathComponent as DefaultMutableTreeNode
                val rootNode = conversationTreeModel.root
                // the message node
                if (currentNode.parent != null && currentNode.parent != rootNode) {
                    val message = currentNode.userObject as Message
                    textArea.text = CacheUtil.getMessage(message.id)
                } else {
                    // the conversation node
                    val conversation = currentNode.userObject as Conversation
                    val conversationId = conversation.id

                    IdUtil.setConversationId(conversationId)

                    val text = CacheUtil.getConversation(conversationId)
                    if (text.isNullOrBlank()) {
                        progressBar.isIndeterminate = true
                        textArea.border = null

                        SwingUtilities.invokeLater {
                            GetConversationContentWorker(this@MainFrame, conversation.id).execute()
                        }
                    } else {
                        textArea.text = text
                    }
                }
            }

            addMouseListener(object : MouseAdapter() {
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
                            progressBar.isIndeterminate = true
                            textArea.border = null

                            SwingUtilities.invokeLater {
                                GetConversationContentWorker(this@MainFrame, conversation.id).execute()
                            }
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            showConversationPopupMenu(e, conversation)
                        }
                    }
                }

                private fun showConversationPopupMenu(e: MouseEvent, conversation: Conversation) {
                    val conversationTreePopupMenu = JPopupMenu().apply {
                        add(JMenuItem(Constant.REFRESH).apply {
                            addActionListener {
                                progressBar.isIndeterminate = true
                                textArea.border = null

                                SwingUtilities.invokeLater {
                                    GetConversationContentWorker(this@MainFrame, conversation.id).execute()
                                }
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

                                progressBar.isIndeterminate = true
                                SwingUtilities.invokeLater {
                                    RenameConversationTitleWorker(
                                        this@MainFrame,
                                        conversationId,
                                        title,
                                    ).execute()
                                }
                            }
                        })

                        add(JMenuItem(Constant.DELETE).apply {
                            addActionListener {
                                val option =
                                    JOptionPane.showConfirmDialog(null, "Do you want to delete this conversion?")
                                if (option != JOptionPane.OK_OPTION) {
                                    return@addActionListener
                                }

                                progressBar.isIndeterminate = true
                                val conversationId = IdUtil.getConversationId()
                                val url = "${ConfigUtil.getServerUrl()}${
                                    String.format(
                                        Constant.URL_DELETE_CONVERSATION,
                                        conversationId
                                    )
                                }"
                                SwingUtilities.invokeLater {
                                    DeleteConversationWorker(this@MainFrame, url).execute()
                                }
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
                                    arrayOf(
                                        Constant.FEEDBACK_LIKE,
                                        Constant.FEEDBACK_DISLIKE,
                                        Constant.FEEDBACK_CANCEL
                                    ),
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

                                progressBar.isIndeterminate = true
                                SwingUtilities.invokeLater {
                                    FeedbackConversationWorker(this@MainFrame, message, rating).execute()
                                }
                            }
                        })
                    }.show(e.component, e.x, e.y)
                }
            })
        }
    }

    private fun initLeftPanel() = JPanel().apply {
        layout = BorderLayout()

        add(JScrollPane(conversationTree).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        })

        add(JPanel().apply {
            layout = GridLayout(3, 1)

            add(JButton(Constant.NEW).apply {
                addActionListener {
                    textArea.border = null
                    textArea.text = ""
                    IdUtil.clearIds()
                    conversationTree.clearSelection()
                }
            })

            add(JButton(Constant.REFRESH).apply {
                addActionListener {
                    progressBar.isIndeterminate = true


                    SwingUtilities.invokeLater {
                        GetConversationListWorker(this@MainFrame).execute()
                    }
                }
            })

            add(JButton(Constant.CLEAR).apply {
                addActionListener {
                    val option = JOptionPane.showConfirmDialog(null, "Do you want to clear all conversions?")
                    if (option != JOptionPane.OK_OPTION) {
                        return@addActionListener
                    }

                    progressBar.isIndeterminate = true
                    SwingUtilities.invokeLater {
                        val url = "${ConfigUtil.getServerUrl()}${Constant.URL_CLEAR_ALL_CONVERSATIONS}"
                        DeleteConversationWorker(this@MainFrame, url).execute()
                    }
                }
            })
        }, BorderLayout.SOUTH)
    }

    private fun initContentField() = JTextField().apply {
        addActionListener {
            val content = contentField.text.trim()
            if (content.isBlank()) {
                "Please input something.".warn()
                return@addActionListener
            }

            progressBar.isIndeterminate = true
            contentField.isEditable = false
            textArea.border = BorderFactory.createTitledBorder(content)
            textArea.text = ""

            SwingUtilities.invokeLater {
                StartConversationWorker(this@MainFrame, content).execute()
            }
        }
    }

    private fun initTextArea() = RSyntaxTextArea().apply {
        isCodeFoldingEnabled = true
        highlightCurrentLine = false
        paintTabLines = true
        lineWrap = true
        antiAliasingEnabled = true
        isEditable = false
    }

    private fun initRightPanel() = JPanel().apply {
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

        val languages = FileTypeUtil.get().defaultContentTypeToFilterMap.keys.sorted().toTypedArray()
        val languageComboBox = JComboBox(languages).apply {
            addItemListener {
                val selected = it.item as String
                textArea.syntaxEditingStyle = selected
            }
            selectedItem = SyntaxConstants.SYNTAX_STYLE_JAVA
        }

        add(languageComboBox, gridBagConstraints.apply {
            gridx = 0
            gridy = 1
            weighty = -1.0
        })

        add(RTextScrollPane(textArea).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }, gridBagConstraints.apply {
            gridx = 0
            gridy = 2
            weighty = 1.0
        })

        ttsButton = JButton(Constant.TTS).apply {
            addActionListener {
                val text = textArea.selectedText
                if (text == null) {
                    "Please select some texts first.".warn()
                    return@addActionListener
                }

                progressBar.isIndeterminate = true
                ttsButton.isEnabled = false
                SwingUtilities.invokeLater {
                    TTSWorker(text, this@MainFrame).execute()
                }
            }
        }

        add(ttsButton, gridBagConstraints.apply {
            gridx = 0
            gridy = 3
            weighty = -1.0
        })
    }

    private fun initMenuBar() = JMenuBar().apply {
        add(JMenu(Constant.MORE).apply {
            add(JMenuItem(Constant.CONFIG).apply {
                addActionListener {
                    val serverUrl = JOptionPane.showInputDialog("Server URL", ConfigUtil.getServerUrl())
                    if (serverUrl == null) {
                        "Please input server url".warn()
                        return@addActionListener
                    }

                    val accessToken = JOptionPane.showInputDialog("Access Token", ConfigUtil.getAccessToken())
                    if (accessToken == null) {
                        "Please input access token".warn()
                        return@addActionListener
                    }

                    ConfigUtil.saveConfig(serverUrl, accessToken)
                }
            })

            add(JMenuItem(Constant.ABOUT).apply {
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
            })
        })
    }
}

fun main() {
    if (System.getProperty("os.name") == "Linux") {
        @Suppress("SpellCheckingInspection")
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
    } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }

    SwingUtilities.invokeLater {
        MainFrame()
    }
}
