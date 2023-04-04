package com.linweiyuan.chatgptswing.worker.chatgpt

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.ConfigUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class RenameConversationTitleWorker(
    private val mainFrame: MainFrame,
    private val conversationId: String,
    private val title: String,
) : SwingWorker<Boolean, Void>() {
    override fun doInBackground(): Boolean {
        try {
            val url = "${ConfigUtil.getServerUrl()}${String.format(Constant.URL_RENAME_TITLE, conversationId)}"
            val requestBody = JSON.toJSONString(mapOf("title" to title))
            val response = Jsoup
                .connect(url)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .preset()
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        SwingUtilities.invokeLater {
            GetConversationListWorker(mainFrame).execute()
        }
    }
}
