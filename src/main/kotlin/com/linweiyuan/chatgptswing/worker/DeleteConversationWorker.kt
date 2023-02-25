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
import javax.swing.SwingWorker

class DeleteConversationWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val mainFrame: MainFrame,
) : SwingWorker<Boolean, Message>() {

    override fun doInBackground(): Boolean {
        mainFrame.progressBar.isIndeterminate = !mainFrame.progressBar.isIndeterminate

        try {
            val response = Jsoup.newSession().useDefault(accessToken)
                .url(String.format(Constant.URL_DELETE_CONVERSATION, conversationId))
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("is_visible" to false)))
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
            IdUtil.clearIds()
            GetConversationListWorker(accessToken, mainFrame).execute()
        }
    }

}