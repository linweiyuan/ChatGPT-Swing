package com.linweiyuan.chatgptswing.worker.chatgpt

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.chatgpt.Message
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.ConfigUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.JOptionPane
import javax.swing.SwingWorker

class FeedbackConversationWorker(
    private val mainFrame: MainFrame,
    private val message: Message,
    private val rating: String,
) : SwingWorker<Boolean, Void>() {
    override fun doInBackground(): Boolean {
        try {
            val url = "${ConfigUtil.getServerUrl()}${Constant.URL_ADD_MESSAGE_FEEDBACK}"
            val requestBody = JSON.toJSONString(
                mapOf(
                    "message_id" to message.id,
                    "conversation_id" to IdUtil.getConversationId(),
                    "rating" to rating,
                )
            )
            val response = Jsoup
                .connect(url)
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
            JOptionPane.showMessageDialog(mainFrame, "Done")
        }
    }
}
