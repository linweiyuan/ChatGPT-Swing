package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.ConversationContentResponse
import com.linweiyuan.chatgptswing.dataclass.ConversationDetail
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.listmodel.ConversationListModel
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Jsoup
import java.awt.Color
import javax.swing.*
import javax.swing.text.StyleConstants

class GetConversationContentWorker(
    private val accessToken: String,
    private val conversation: Conversation,
    private val progressBar: JProgressBar,
    private val chatPane: JTextPane,
    private val conversationList: JList<Conversation>
) : SwingWorker<Void, Message>() {

    override fun doInBackground(): Void? {
        progressBar.isIndeterminate = !progressBar.isIndeterminate
        chatPane.border = BorderFactory.createTitledBorder(conversation.title)
        chatPane.text = ""

        val connection = Jsoup.newSession().useDefault(accessToken)
        val response = connection.url("https://apps.openai.com/api/conversation/${conversation.id}").execute()
        if (response.statusCode() != Constant.HTTP_OK) {
            "Failed to get conversation content.".warn()
            return null
        }

        val chatContentResponse = JSON.parseObject(response.body(), ConversationContentResponse::class.java)
        val mapping = chatContentResponse.mapping
        val currentNode = chatContentResponse.currentNode
        IdUtil.setParentMessageId(currentNode)
        handleConversationDetail(mapping, currentNode)

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
        val doc = chatPane.styledDocument
        val style = chatPane.addStyle("", null)
        chunks.reversed().forEach {
            if (it.author.role == Constant.ROLE_USER) {
                StyleConstants.setForeground(style, Color.WHITE)
                doc.insertString(0, it.content.parts[0] + "\n\n", style)
            } else {
                StyleConstants.setForeground(style, Color.GREEN)
                doc.insertString(0, it.content.parts[0] + "\n\n", style)
            }
        }
    }

    override fun done() {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        val conversationListModel = conversationList.model as ConversationListModel
        val indexByConversationId = conversationListModel.getIndexByConversationId(conversation.id)
        conversationList.selectedIndex = indexByConversationId
    }

}