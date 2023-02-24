package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.GenerateTitleResponse
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.JProgressBar
import javax.swing.JTree
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GenTitleWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val messageId: String,
    private val progressBar: JProgressBar,
    private val conversationTree: JTree,
) : SwingWorker<DefaultMutableTreeNode, String>() {

    private val conversationTreeModel = conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode

    override fun doInBackground(): DefaultMutableTreeNode? {
        try {
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(String.format(Constant.URL_GEN_CONVERSATION_TITLE, conversationId))
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("message_id" to messageId)))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return null
            }

            IdUtil.setConversationId(conversationId)
            val generateTitleResponse = JSON.parseObject(response.body(), GenerateTitleResponse::class.java)
            val node = DefaultMutableTreeNode(Conversation(conversationId, generateTitleResponse.title))
            conversationTreeRoot.add(node)
            return node
        } catch (e: Exception) {
            e.toString().warn()
            return null
        }
    }

    override fun done() {
        progressBar.isIndeterminate = false

        val node = get()
        if (node != null) {
            with(conversationTreeModel) {
                reload()
                conversationTree.selectionPath = TreePath(getPathToRoot(node))
            }
        }
    }

}