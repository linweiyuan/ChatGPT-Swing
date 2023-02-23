package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.ChatResponse
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.toHtml
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.IdUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.*
import javax.swing.*

class ChatWorker(
    private val accessToken: String,
    private val progressBar: JProgressBar,
    private val contentField: JTextField,
    private val chatPane: JTextPane,
    private val conversationList: JList<Conversation>,
) : SwingWorker<Conversation, String>() {

    private var conversationId = IdUtil.getConversationId()
    private var parentMessageId = IdUtil.getParentMessageId()

    override fun doInBackground(): Conversation? {
        val content = contentField.text.trim()
        chatPane.contentType = Constant.TEXT_PLAIN
        progressBar.isIndeterminate = true
        contentField.isEditable = !contentField.isEditable
        contentField.text = ""
        chatPane.border = BorderFactory.createTitledBorder(content)
        chatPane.text = ""

        try {
            val messageId = UUID.randomUUID().toString()
            val requestMap = mapOf(
                "messageId" to messageId,
                "parentMessageId" to parentMessageId.ifBlank { UUID.randomUUID().toString() },
                "conversationId" to conversationId.ifBlank { null },
                "content" to content,
            )
            val response = Jsoup.newSession().useDefault(accessToken).newRequest()
                .url(Constant.URL_MAKE_CONVERSATION)
                .method(Connection.Method.POST)
                .requestBody(JSON.toJSONString(requestMap))
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return null
            }

            response.bodyStream().bufferedReader().use {
                var line = it.readLine()
                while (line != null) {
                    if (line == "") {
                        line = it.readLine()
                        continue
                    } else if (line == "event: ping") {
                        it.readLine() // time
                        it.readLine() // \n
                        line = it.readLine()
                        continue
                    } else if (line == "data: [DONE]") {
                        break
                    }

                    // remove "data: ", length is 6
                    val chatResponse = JSON.parseObject(line.substring(6), ChatResponse::class.java)
                    if (conversationId.isBlank()) {
                        conversationId = chatResponse.conversationId
                    }
                    if (parentMessageId.isBlank()) {
                        IdUtil.setParentMessageId(chatResponse.message.id)
                    }
                    val part = chatResponse.message.content.parts[0]
                    if (part.isNotBlank()) {
                        publish(chatResponse.message.content.parts[0])
                    }

                    line = it.readLine()
                }
            }

            if (IdUtil.getConversationId().isBlank()) {
                SwingUtilities.invokeAndWait {
                    GenTitleWorker(
                        accessToken,
                        conversationId,
                        messageId,
                        progressBar,
                        conversationList
                    ).execute()
                }
            }
        } catch (e: Exception) {
            e.toString().warn()
        }

        return null
    }

    override fun process(chunks: MutableList<String>) {
        // prefer not to use a loop
        chatPane.text = chunks[chunks.size - 1]
    }

    override fun done() {
        progressBar.isIndeterminate = false
        contentField.isEditable = !contentField.isEditable

        val html = chatPane.text.toHtml()
        chatPane.contentType = Constant.TEXT_HTML // this line will clear all contents
        chatPane.text = html

        SwingUtilities.invokeLater {
            GetConversationContentWorker(
                accessToken,
                conversationId,
                progressBar,
                chatPane,
            ).execute()
        }
    }

}