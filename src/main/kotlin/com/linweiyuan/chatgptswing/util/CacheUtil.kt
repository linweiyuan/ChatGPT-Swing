package com.linweiyuan.chatgptswing.util

import com.linweiyuan.chatgptswing.dataclass.api.Message

object CacheUtil {
    private var conversationMap = mutableMapOf<String, String>()
    private var messageMap = mutableMapOf<String, String>()

    private var apiMessages = mutableListOf<Message>()

    fun setConversation(conversationId: String, text: String) {
        conversationMap[conversationId] = text
    }

    fun getConversation(conversationId: String) = conversationMap[conversationId]

    fun setMessage(messageId: String, text: String) {
        messageMap[messageId] = text
    }

    fun getMessage(messageId: String) = messageMap[messageId]

    fun getApiMessages() = apiMessages
}
