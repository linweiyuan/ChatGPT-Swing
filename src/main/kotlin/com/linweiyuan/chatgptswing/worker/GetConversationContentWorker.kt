package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.ConversationContentResponse
import com.linweiyuan.chatgptswing.dataclass.ConversationDetail
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.toHtml
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.JList
import javax.swing.JProgressBar
import javax.swing.JTextPane
import javax.swing.SwingWorker

class GetConversationContentWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val progressBar: JProgressBar,
    private val chatPane: JTextPane,
    private val conversationList: JList<Conversation>
) : SwingWorker<Void, Message>() {

    private val contentBuilder = StringBuilder()

    override fun doInBackground(): Void? {
        progressBar.isIndeterminate = !progressBar.isIndeterminate
        chatPane.border = null
        chatPane.text = ""

        try {
            val connection = Jsoup.newSession().useDefault(accessToken)

            val response = connection.newRequest()
                .url("https://apps.openai.com/api/conversation/$conversationId")
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                "Failed to get conversation content.".warn()
                return null
            }

            val chatContentResponse = JSON.parseObject(response.body(), ConversationContentResponse::class.java)
            val mapping = chatContentResponse.mapping
            val currentNode = chatContentResponse.currentNode
            IdUtil.setParentMessageId(currentNode)
            handleConversationDetail(mapping, currentNode)
        } catch (e: Exception) {
            e.toString().warn()
        }

        return null
    }

    private fun handleConversationDetail(mapping: Map<String, ConversationDetail>, id: String) {
        val chatDetail = mapping.getValue(id)
        val parentId = chatDetail.parent
        if (parentId != null) {
            handleConversationDetail(mapping, parentId)
        }

        val message = chatDetail.message
        if (message != null) {
            publish(message)
        }
    }

    override fun process(chunks: MutableList<Message>) {
        chunks.forEach {
            val content = it.content.parts[0]
            if (it.author.role == Constant.ROLE_USER) {
                contentBuilder.append(Constant.DIV_BACKGROUND_COLOR_PREFIX_USER)
            } else {
                contentBuilder.append(Constant.DIV_BACKGROUND_COLOR_PREFIX_ASSISTANT)
            }
            contentBuilder.append(content).append(Constant.DIV_POSTFIX)
            contentBuilder.append(Constant.HTML_NEW_LINE)
        }
    }

    override fun done() {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        conversationList.selectedIndex = (conversationList.model as ConversationListModel)
            .getIndexByConversationId(conversationId)

        val html = contentBuilder.toString().toHtml()
        chatPane.contentType = Constant.TEXT_HTML
        chatPane.text = html
    }

}