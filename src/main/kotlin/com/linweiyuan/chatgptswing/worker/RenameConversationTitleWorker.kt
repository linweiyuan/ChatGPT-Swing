package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.JList
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class RenameConversationTitleWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val title: String,
    private val progressBar: JProgressBar,
    private val conversationList: JList<Conversation>
) : SwingWorker<Boolean, Message>() {

    override fun doInBackground(): Boolean {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        try {
            val response = Jsoup.newSession().useDefault(accessToken)
                .url(String.format(Constant.URL_RENAME_CONVERSATION, conversationId))
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("title" to title)))
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
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        SwingUtilities.invokeLater {
            GetConversationListWorker(accessToken, progressBar, conversationList).execute()
        }
    }

}