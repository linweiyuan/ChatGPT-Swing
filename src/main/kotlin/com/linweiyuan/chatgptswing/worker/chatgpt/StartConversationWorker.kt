package com.linweiyuan.chatgptswing.worker.chatgpt

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.chatgpt.Author
import com.linweiyuan.chatgptswing.dataclass.chatgpt.Content
import com.linweiyuan.chatgptswing.dataclass.chatgpt.ConversationSSE
import com.linweiyuan.chatgptswing.dataclass.chatgpt.Message
import com.linweiyuan.chatgptswing.extensions.getCurrentNode
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.ConfigUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.*
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class StartConversationWorker(
    private val mainFrame: MainFrame,
    private val content: String,
) : SwingWorker<Boolean, String>() {
    private var conversationId = IdUtil.getConversationId()

    private val conversationTreeModel = mainFrame.conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode
    private val currentTreeNode = conversationTreeRoot.getCurrentNode(conversationId)

    private lateinit var newMessageNode: DefaultMutableTreeNode
    private val messageId = UUID.randomUUID().toString()

    override fun doInBackground(): Boolean {
        try {
            val startConversationRequest = mapOf(
                "action" to "next",
                "messages" to listOf(
                    mapOf(
                        "author" to mapOf(
                            "role" to "user"
                        ),
                        "content" to mapOf(
                            "content_type" to "text",
                            "parts" to listOf(content),
                        ),
                        "id" to UUID.randomUUID().toString(),
                        "role" to "user",
                    )
                ),
                "model" to Constant.MODEL_CHATGPT,
                "parent_message_id" to IdUtil.getParentMessageId().ifBlank { UUID.randomUUID().toString() },
                "conversation_id" to conversationId.ifBlank { null },
                "continue_text" to "continue",
            )

            IdUtil.setParentMessageId("")

            val url = "${ConfigUtil.getServerUrl()}${Constant.URL_START_CONVERSATION}"
            val requestBody = JSON.toJSONString(startConversationRequest)
            val response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .preset()
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage(mainFrame)
                return false
            }

            currentTreeNode?.let {
                newMessageNode = DefaultMutableTreeNode(
                    Message(
                        id = messageId,
                        author = Author(Constant.ROLE_USER),
                        content = Content(Constant.MESSAGE_CONTENT_TYPE_TEXT, mutableListOf(content)),
                    )
                )
                it.add(newMessageNode)
            }

            response.bodyStream().bufferedReader().use {
                var line = it.readLine()
                while (line != null) {
                    if (line == "" || line.startsWith("event")) {
                        line = it.readLine()
                        continue
                    } else if (line.trim().endsWith("[DONE]")) {
                        break
                    }

                    val conversationSSE = JSON.parseObject(line.substring(5), ConversationSSE::class.java)
                    if (conversationId.isBlank()) {
                        conversationId = conversationSSE.conversationId
                    }
                    if (IdUtil.getParentMessageId().isBlank()) {
                        IdUtil.setParentMessageId(conversationSSE.message.id)
                    }
                    val part = conversationSSE.message.content.parts[0]
                    if (part.isNotBlank()) {
                        publish(conversationSSE.message.content.parts[0])
                    }

                    line = it.readLine()
                }
            }

            if (IdUtil.getConversationId().isBlank()) {
                SwingUtilities.invokeLater {
                    GenerateTitleWorker(mainFrame, conversationId, IdUtil.getParentMessageId()).execute()
                }
            }

            return true
        } catch (e: Exception) {
            e.toString().warn(mainFrame)
            return false
        }
    }

    override fun process(chunks: MutableList<String>) {
        chunks.forEach { mainFrame.textArea.text = it }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false
        mainFrame.contentField.isEditable = true

        val conversation = get()
        if (conversation != null) {
            mainFrame.contentField.text = ""

            if (IdUtil.getConversationId().isNotBlank()) {
                CacheUtil.setMessage(messageId, mainFrame.textArea.text)
                CacheUtil.setConversation(conversationId, "") // force it to reload to get new messages

                with(conversationTreeModel) {
                    reload()
                    mainFrame.conversationTree.selectionPath = TreePath(getPathToRoot(newMessageNode))
                }
            }
        }
    }
}
