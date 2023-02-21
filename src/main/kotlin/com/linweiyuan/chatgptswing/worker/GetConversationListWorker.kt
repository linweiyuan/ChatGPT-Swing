package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.ConversationListResponse
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Jsoup
import javax.swing.JList
import javax.swing.SwingWorker

class GetConversationListWorker(
    private val accessToken: String,
    conversationList: JList<Conversation>
) : SwingWorker<Void, Void>() {

    private val conversationListModel = conversationList.model as ConversationListModel

    override fun doInBackground(): Void? {
        val connection = Jsoup.newSession().useDefault(accessToken)

        val response = connection
            .url("https://apps.openai.com/api/conversations?offset=0&limit=${Constant.CONVERSATION_LIST_FETCH_COUNT}")
            .execute()
        if (response.statusCode() != Constant.HTTP_OK) {
            "Failed to get conversation list.".warn()
            return null
        }

        conversationListModel.clear()
        JSON.parseObject(response.body(), ConversationListResponse::class.java).items.forEach {
            conversationListModel.addItem(it)
        }
        conversationListModel.done()

        return null
    }

}