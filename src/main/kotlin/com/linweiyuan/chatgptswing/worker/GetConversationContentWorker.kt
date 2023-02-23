package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.ConversationContentResponse
import com.linweiyuan.chatgptswing.dataclass.ConversationDetail
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.toHtml
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import javax.swing.JProgressBar
import javax.swing.JTextPane
import javax.swing.SwingWorker

class GetConversationContentWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val progressBar: JProgressBar,
    private val chatPane: JTextPane,
) : SwingWorker<Boolean, Message>() {

    private val contentBuilder = StringBuilder()

    override fun doInBackground(): Boolean {
        progressBar.isIndeterminate = !progressBar.isIndeterminate
        chatPane.border = null

        try {
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(String.format(Constant.URL_GET_CONVERSATION_CONTENT, conversationId))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            val chatContentResponse = JSON.parseObject(response.body(), ConversationContentResponse::class.java)
            val mapping = chatContentResponse.mapping
            val currentNode = chatContentResponse.currentNode
            IdUtil.setParentMessageId(currentNode)

            handleConversationDetail(mapping, currentNode)

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
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

        val ok = get()
        if (ok) {
            val html = contentBuilder.toString().toHtml()
            chatPane.contentType = Constant.TEXT_HTML
            chatPane.text = html

            CacheUtil.setConversation(conversationId, html)
        }
    }

}