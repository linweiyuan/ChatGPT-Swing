package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.ConversationContentResponse
import com.linweiyuan.chatgptswing.dataclass.ConversationDetail
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.getCurrentNode
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GetConversationContentWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val mainFrame: MainFrame,
) : SwingWorker<Boolean, Message>() {

    private val messages = mutableListOf<Message>()
    private val conversationTreeModel = mainFrame.conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode
    private val currentTreeNode = conversationTreeRoot.getCurrentNode(conversationId)

    override fun doInBackground(): Boolean {
        try {
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(String.format(Constant.URL_GET_CONVERSATION_CONTENT, conversationId))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            val chatContentResponse = JSON.parseObject(response.body(), ConversationContentResponse::class.java)
            val mapping = chatContentResponse.mapping
            val currentNode = chatContentResponse.currentNode
            IdUtil.setParentMessageId(currentNode)

            currentTreeNode?.removeAllChildren()
            handleConversationDetail(mapping, currentNode)

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    private fun handleConversationDetail(mapping: Map<String, ConversationDetail>, id: String) {
        val chatDetail = mapping.getValue(id)
        val parentId = chatDetail.parent
        if (parentId != null) {
            CacheUtil.setMessage(parentId, chatDetail.message!!.content.parts[0])
            handleConversationDetail(mapping, parentId)
        }

        val message = chatDetail.message
        if (message != null) {
            publish(message)
        }
    }

    override fun process(chunks: MutableList<Message>) {
        chunks.forEach {
            val content = it.content.parts[0]
            if (content.isNotBlank()) {
                if (it.author.role == Constant.ROLE_USER) {
                    // if start a new conversation, currentTreeNode is null
                    currentTreeNode?.add(DefaultMutableTreeNode(it))
                }
                messages.add(it)
            }
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        val ok = get()
        if (ok) {
            val text = messages.joinToString(separator = "\n\n")
            mainFrame.textArea.text = text

            CacheUtil.setConversation(conversationId, text)

            with(conversationTreeModel) {
                reload()
                val conversationId = IdUtil.getConversationId()
                if (conversationId.isNotBlank()) {
                    val highlightedNode = conversationTreeRoot.getCurrentNode(conversationId)
                    mainFrame.conversationTree.selectionPath = TreePath(getPathToRoot(highlightedNode))
                }
            }
        }
    }

}