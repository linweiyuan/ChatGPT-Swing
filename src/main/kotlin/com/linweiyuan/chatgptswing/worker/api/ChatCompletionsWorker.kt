package com.linweiyuan.chatgptswing.worker.api

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.api.ChatCompletionsRequest
import com.linweiyuan.chatgptswing.dataclass.api.ChatCompletionsSSE
import com.linweiyuan.chatgptswing.dataclass.api.Message
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.showErrorMessage
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.CacheUtil
import com.linweiyuan.chatgptswing.util.ConfigUtil
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.swing.SwingWorker

class ChatCompletionsWorker(
    private val mainFrame: MainFrame,
    private val content: String,
) : SwingWorker<Boolean, String>() {
    override fun doInBackground(): Boolean {
        try {
            val chatCompletionsRequest = ChatCompletionsRequest(
                messages = CacheUtil.getApiMessages().apply {
                    add(Message(content = content))
                })
            val url = "${ConfigUtil.getServerUrl()}${Constant.URL_API_CHAT_COMPLETIONS}"
            val requestBody = JSON.toJSONString(chatCompletionsRequest)
            val response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .requestBody(requestBody)
                .preset(false)
                .execute()
            if (response.statusCode() != Constant.HTTP_OK) {
                response.showErrorMessage()
                return false
            }

            response.bodyStream().bufferedReader().use {
                var line = it.readLine()
                while (line != null) {
                    if (line == "" || line.startsWith("event")) {
                        line = it.readLine()
                        continue
                    } else if (line.trim().endsWith("[DONE]")) {
                        break
                    }

                    val chatCompletionsSSE = JSON.parseObject(line.substring(5), ChatCompletionsSSE::class.java)
                    val choice = chatCompletionsSSE.choices[0]
                    choice.finishReason?.let { reason ->
                        if (reason == "stop") {
                            return@use
                        }
                    }
                    choice.delta.content?.let { content ->
                        publish(content)
                    }

                    line = it.readLine()
                }
            }

            return true
        } catch (e: Exception) {
            e.toString().warn()
            return false
        }
    }

    override fun process(chunks: MutableList<String>) {
        chunks.forEach { mainFrame.textArea.append(it) }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false
        mainFrame.contentField.isEditable = true

        val ok = get()
        if (ok != null) {
            mainFrame.contentField.text = ""

            CacheUtil.getApiMessages().add(Message(role = Constant.ROLE_ASSISTANT, content = mainFrame.textArea.text))
        }
    }
}
