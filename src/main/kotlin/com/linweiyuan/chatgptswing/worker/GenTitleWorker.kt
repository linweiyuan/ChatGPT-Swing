package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.GenerateTitleResponse
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.JList
import javax.swing.JProgressBar
import javax.swing.SwingWorker

class GenTitleWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val messageId: String,
    private val progressBar: JProgressBar,
    private val conversationList: JList<Conversation>,
) : SwingWorker<Boolean, String>() {

    private val conversationListModel = conversationList.model as ConversationListModel

    override fun doInBackground(): Boolean {
        try {
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(String.format(Constant.URL_GEN_CONVERSATION_TITLE, conversationId))
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(mapOf("message_id" to messageId)))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            IdUtil.setConversationId(conversationId)
            val generateTitleResponse = JSON.parseObject(response.body(), GenerateTitleResponse::class.java)
            conversationListModel.addItem(Conversation(conversationId, generateTitleResponse.title))

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun done() {
        progressBar.isIndeterminate = false

        val ok = get()
        if (ok) {
            conversationListModel.update()

            val conversationListModel = conversationList.model as ConversationListModel
            conversationList.selectedIndex = conversationListModel.getIndexByConversationId(conversationId)
        }
    }

}