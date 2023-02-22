package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.*
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
    private val chatPane: JTextPane
) : SwingWorker<Void, String>() {

    private var conversationId = IdUtil.getConversationId()
    private var parentMessageId = IdUtil.getParentMessageId()

    override fun doInBackground(): Void? {
        val content = contentField.text.trim()

        chatPane.contentType = Constant.TEXT_PLAIN
        progressBar.isIndeterminate = true
        contentField.isEditable = !contentField.isEditable
        contentField.text = ""
        chatPane.border = BorderFactory.createTitledBorder(content)
        chatPane.text = ""

        val connection = Jsoup.newSession().useDefault(accessToken)

        val chatRequest = ChatRequest(
            messages = listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    author = Author(Constant.ROLE_USER),
                    content = Content(parts = mutableListOf(content))
                )
            ),
        )
        if (conversationId.isNotBlank()) {
            chatRequest.conversationId = conversationId
        }
        if (parentMessageId.isNotBlank()) {
            chatRequest.parentMessageId = parentMessageId
        }

        val response = connection.url("https://apps.openai.com/api/conversation")
            .method(Connection.Method.POST)
            .requestBody(JSON.toJSONString(chatRequest))
            .execute()
        if (response.statusCode() == Constant.HTTP_TOO_MANY_REQUESTS) {
            "Too many requests, please try again later.".warn()
            return null
        } else if (response.statusCode() == Constant.HTTP_INTERNAL_SERVER_ERROR) {
            "Server error, please try again later.".warn()
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

                val chatResponse = JSON.parseObject(line.substring(6), ChatResponse::class.java) // remove "data: "
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
    }

}