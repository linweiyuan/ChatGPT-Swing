package com.linweiyuan.chatgptswing.worker.chatgpt

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.chatgpt.ConversationList
import com.linweiyuan.chatgptswing.extensions.getCurrentNode
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.ConfigUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GetConversationListWorker(
    private val mainFrame: MainFrame,
) : SwingWorker<Boolean, Void>() {
    private val conversationTreeModel = mainFrame.conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode

    override fun doInBackground(): Boolean {
        try {
            val url = "${ConfigUtil.getServerUrl()}${String.format(Constant.URL_GET_CONVERSATION_LIST, 0, 100)}"
            val response = Jsoup.connect(url).preset().execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            conversationTreeRoot.removeAllChildren()
            JSON.parseObject(response.body(), ConversationList::class.java).items.forEach {
                conversationTreeRoot.add(DefaultMutableTreeNode(it))
                CacheUtil.setConversation(it.id, "")
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
                        GetConversationContentWorker(mainFrame, conversationId).execute()
                    }
                }
            }
        }
    }
}
