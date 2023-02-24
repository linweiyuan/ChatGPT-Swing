package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.ConversationListResponse
import com.linweiyuan.chatgptswing.extensions.getCurrentNode
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.JProgressBar
import javax.swing.JTree
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GetConversationListWorker(
    private val accessToken: String,
    private val progressBar: JProgressBar,
    private val conversationTree: JTree,
) : SwingWorker<Boolean, Void>() {

    private val conversationTreeModel = conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode

    override fun doInBackground(): Boolean {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

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
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        val ok = get()
        if (ok) {
            with(conversationTreeModel) {
                reload()
                val conversationId = IdUtil.getConversationId()
                if (conversationId.isNotBlank()) {
                    val highlightedNode = conversationTreeRoot.getCurrentNode(conversationId)
                    conversationTree.selectionPath = TreePath(getPathToRoot(highlightedNode))
                }
            }
        }
    }

}