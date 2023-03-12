package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.ConfigUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.time.LocalDateTime
import javax.swing.SwingWorker
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class GenerateTitleWorker(
    private val mainFrame: MainFrame,
    private val conversationId: String,
    private val parentMessageId: String,
) : SwingWorker<DefaultMutableTreeNode, String>() {
    private val conversationTreeModel = mainFrame.conversationTree.model as DefaultTreeModel
    private val conversationTreeRoot = conversationTreeModel.root as DefaultMutableTreeNode

    override fun doInBackground(): DefaultMutableTreeNode? {
        try {
            val url = "${ConfigUtil.getServerUrl()}${String.format(Constant.URL_GENERATE_TITLE, conversationId)}"
            val requestBody = JSON.toJSONString(
                mapOf(
                    "message_id" to parentMessageId,
                    "model" to Constant.MODEL,
                )
            )
            val response = Jsoup
                .connect(url)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .preset()
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return null
            }

            IdUtil.setConversationId(conversationId)

            val node = DefaultMutableTreeNode(
                Conversation(
                    createTime = LocalDateTime.now().toString(),
                    id = conversationId,
                    title = JSON.parseObject(response.body()).getString("title")
                )
            )
            conversationTreeRoot.add(node)
            return node
        } catch (e: Exception) {
            e.toString().warn()
            return null
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        val node = get()
        if (node != null) {
            with(conversationTreeModel) {
                reload()
                mainFrame.conversationTree.selectionPath = TreePath(getPathToRoot(node))
            }
        }
    }
}
