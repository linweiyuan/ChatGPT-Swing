package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.JOptionPane
import javax.swing.SwingWorker

class FeedbackConversationWorker(
    private val accessToken: String,
    private val message: Message,
    private val rating: String,
    private val mainFrame: MainFrame,
) : SwingWorker<Boolean, Message>() {

    override fun doInBackground(): Boolean {
        mainFrame.progressBar.isIndeterminate = !mainFrame.progressBar.isIndeterminate

        try {
            val response = Jsoup.newSession().useDefault(accessToken)
                .url(Constant.URL_ADD_MESSAGE_FEEDBACK)
                .method(Connection.Method.POST)
                .requestBody(
                    JSON.toJSONString(
                        mapOf(
                            "message_id" to message.id,
                            "conversation_id" to IdUtil.getConversationId(),
                            "rating" to rating,
                        )
                    )
                )
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
        mainFrame.progressBar.isIndeterminate = !mainFrame.progressBar.isIndeterminate

        val ok = get()
        if (ok) {
            JOptionPane.showMessageDialog(null, "Done")
        }
    }

}