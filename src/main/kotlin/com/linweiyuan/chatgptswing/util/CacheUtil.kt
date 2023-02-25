package com.linweiyuan.chatgptswing.util

object CacheUtil {
    private var conversationMap = mutableMapOf<String, String>()
    private var messageMap = mutableMapOf<String, String>()

    fun setConversation(conversationId: String, text: String) {
        conversationMap[conversationId] = text
    }

    fun getConversation(conversationId: String) = conversationMap[conversationId]

    fun setMessage(messageId: String, text: String) {
        messageMap[messageId] = text
    }

    fun getMessage(messageId: String) = messageMap[messageId]
}