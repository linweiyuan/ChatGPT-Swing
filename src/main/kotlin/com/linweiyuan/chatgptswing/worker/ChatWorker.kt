package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.*
import com.linweiyuan.chatgptswing.extensions.*
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class ChatWorker(
    private val accessToken: String,
    private val progressBar: JProgressBar,
    private val contentField: JTextField,
    private val chatPane: JTextPane,
    private val conversationTree: JTree
) : SwingWorker<Conversation, String>() {

    private var conversationId = IdUtil.getConversationId()
    private var parentMessageId = IdUtil.getParentMessageId()

    private val conversationTreeModel = conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode
    private val currentTreeNode = conversationTreeRoot.getCurrentNode(conversationId)

    private lateinit var newMessageNode: DefaultMutableTreeNode
    private val messageId = UUID.randomUUID().toString()

    override fun doInBackground(): Conversation? {
        val content = contentField.text.trim()
        chatPane.contentType = Constant.TEXT_PLAIN
        progressBar.isIndeterminate = true
        contentField.isEditable = !contentField.isEditable
        contentField.text = ""
        chatPane.border = BorderFactory.createTitledBorder(content)
        chatPane.text = ""

        try {
            val requestMap = mapOf(
                "message_Id" to messageId,
                "parent_message_id" to parentMessageId.ifBlank { UUID.randomUUID().toString() },
                "conversation_id" to conversationId.ifBlank { null },
                "content" to content,
            )
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(Constant.URL_MAKE_CONVERSATION)
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(requestMap))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return null
            }

            currentTreeNode?.let {
                newMessageNode = DefaultMutableTreeNode(
                    Message(
                        messageId,
                        Author(Constant.ROLE_USER),
                        Content(Constant.MESSAGE_CONTENT_TYPE_TEXT, mutableListOf(content))
                    )
                )
                it.add(newMessageNode)
            }

            response.bodyStream().bufferedReader().use {
                var line = it.readLine()
                while (line != null) {
                    if (line == "") {
                        line = it.readLine()
                        continue
                    } else if (line == "event: ping") {
                        it.readLine() // time
                        it.readLine() // \n
                        line = it.readLine()
                        continue
                    } else if (line == "data: [DONE]") {
                        break
                    }

                    // remove "data: ", length is 6
                    val chatResponse = JSON.parseObject(line.substring(6), ChatResponse::class.java)
                    if (conversationId.isBlank()) {
                        conversationId = chatResponse.conversationId
                    }
                    if (parentMessageId.isBlank()) {
                        IdUtil.setParentMessageId(chatResponse.message.id)
                    }
                    val part = chatResponse.message.content.parts[0]
                    if (part.isNotBlank()) {
                        publish(chatResponse.message.content.parts[0])
                    }

                    line = it.readLine()
                }
            }

            if (IdUtil.getConversationId().isBlank()) {
                SwingUtilities.invokeLater {
                    GenTitleWorker(
                        accessToken,
                        conversationId,
                        messageId,
                        progressBar,
                        conversationTree,
                    ).execute()
                }
            }

            return Conversation(conversationId, "")
        } catch (e: Exception) {
            e.toString().warn()
            return null
        }
    }

    override fun process(chunks: MutableList<String>) {
        chunks.forEach { chatPane.text = it }
    }

    override fun done() {
        progressBar.isIndeterminate = false
        contentField.isEditable = !contentField.isEditable

        val conversation = get()
        if (conversation != null) {
            if (IdUtil.getConversationId().isNotBlank()) {
                CacheUtil.setMessage(messageId, chatPane.text.toHtml())

                with(conversationTreeModel) {
                    reload()
                    conversationTree.selectionPath = TreePath(getPathToRoot(newMessageNode))
                }
            }
        }
    }

}