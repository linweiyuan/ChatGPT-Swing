package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.ConversationListResponse
import com.linweiyuan.chatgptswing.extensions.getCurrentNode
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GetConversationListWorker(
    private val accessToken: String,
    private val mainFrame: MainFrame,
) : SwingWorker<Boolean, Void>() {

    private val conversationTreeModel = mainFrame.conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode

    override fun doInBackground(): Boolean {
        try {
            val response = Jsoup.newSession().useDefault(accessToken).url(Constant.URL_GET_CONVERSATION_LIST).execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            conversationTreeRoot.removeAllChildren()
            JSON.parseObject(response.body(), ConversationListResponse::class.java).items.forEach {
                conversationTreeRoot.add(DefaultMutableTreeNode(it))
            }

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        val ok = get()
        if (ok) {
            mainFrame.textArea.text = null
            with(conversationTreeModel) {
                reload()
                val conversationId = IdUtil.getConversationId()
                if (conversationId.isNotBlank()) {
                    val highlightedNode = conversationTreeRoot.getCurrentNode(conversationId)
                    mainFrame.conversationTree.selectionPath = TreePath(getPathToRoot(highlightedNode))
                    SwingUtilities.invokeLater {
                        GetConversationContentWorker(accessToken, conversationId, mainFrame).execute()
                    }
                }
            }
        }
    }

}