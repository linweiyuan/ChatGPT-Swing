package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.ChatRequest
import com.linweiyuan.chatgptswing.dataclass.ChatResponse
import com.linweiyuan.chatgptswing.dataclass.Content
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.toHtml
import com.linweiyuan.chatgptswing.extensions.useDefault
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
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

    private val content = contentField.text.trim()
    override fun doInBackground(): Void? {
        chatPane.contentType = Constant.TEXT_PLAIN
        progressBar.isVisible = true
        contentField.parent.isVisible = false
        contentField.text = ""
        chatPane.border = BorderFactory.createTitledBorder(content)
        chatPane.text = ""

        val connection = Jsoup.newSession().useDefault().headers(
            mapOf(
                Constant.CONTENT_TYPE to Constant.APPLICATION_JSON,
                Constant.AUTHORIZATION to "${Constant.BEARER} $accessToken",
            )
        )

        val chatRequest = ChatRequest(
            parentMessageId = UUID.randomUUID(),
            messages = listOf(
                Message(
                    id = UUID.randomUUID(),
                    role = "user",
                    content = Content(parts = listOf(content))
                )
            )
        )

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
        progressBar.isVisible = false
        contentField.parent.isVisible = true

        val html = chatPane.text.toHtml()
        chatPane.contentType = Constant.TEXT_HTML // this line will clear all contents
        chatPane.text = html
    }

}