package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.dataclass.Message
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.swing.JList
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

class RenameConversationTitleWorker(
    private val accessToken: String,
    private val conversationId: String,
    private val title: String,
    private val progressBar: JProgressBar,
    private val conversationList: JList<Conversation>
) : SwingWorker<Void, Message>() {

    override fun doInBackground(): Void? {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://apps.openai.com/api/conversation/$conversationId")
                .header(Constant.AUTHORIZATION, accessToken)
                .patch(JSON.toJSONString(JSONObject().apply {
                    put("title", title)
                }).toRequestBody(Constant.APPLICATION_JSON.toMediaType()))
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                "Failed to rename conversation.".warn()
                return null
            }

        } catch (e: Exception) {
            e.toString().warn()
        }

        return null
    }

    override fun done() {
        progressBar.isIndeterminate = !progressBar.isIndeterminate

        SwingUtilities.invokeLater {
            GetConversationListWorker(accessToken, progressBar, conversationList).execute()
        }
    }

}