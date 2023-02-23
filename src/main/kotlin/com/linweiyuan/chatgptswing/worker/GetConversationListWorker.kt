package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.ConversationListResponse
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.JList
import javax.swing.JProgressBar
import javax.swing.SwingWorker

class GetConversationListWorker(
    private val accessToken: String,
    private val progressBar: JProgressBar,
    private val conversationList: JList<Conversation>
) : SwingWorker<Boolean, Void>() {

    private val conversationListModel = conversationList.model as ConversationListModel

    override fun doInBackground(): Boolean {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        try {
            val response = Jsoup.newSession().useDefault(accessToken).url(Constant.URL_GET_CONVERSATION_LIST).execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            conversationListModel.clear()
            JSON.parseObject(response.body(), ConversationListResponse::class.java).items.forEach {
                conversationListModel.addItem(it)
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
            conversationListModel.update()
            conversationList.selectedIndex = conversationListModel.getIndexByConversationId(IdUtil.getConversationId())
        }
    }

}