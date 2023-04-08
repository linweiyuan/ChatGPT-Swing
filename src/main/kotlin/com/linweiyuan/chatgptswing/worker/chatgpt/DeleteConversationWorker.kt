package com.linweiyuan.chatgptswing.worker.chatgpt

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class DeleteConversationWorker(
    private val mainFrame: MainFrame,
    private val url: String,
) : SwingWorker<Boolean, Void>() {

    override fun doInBackground(): Boolean {
        try {
            val requestBody = JSON.toJSONString(mapOf("is_visible" to false))
            val response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .preset()
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage(mainFrame)
                return false
            }

            return true
        } catch (e: Exception) {
            e.toString().warn(mainFrame)
            return false
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        val ok = get()
        if (ok) {
            IdUtil.clearIds()
            SwingUtilities.invokeLater {
                GetConversationListWorker(mainFrame).execute()
            }
        }
    }
}
