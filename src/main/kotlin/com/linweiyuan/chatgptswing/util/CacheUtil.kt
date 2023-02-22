package com.linweiyuan.chatgptswing.util

object CacheUtil {
    private var conversationMap = mutableMapOf<String, String>()

    fun setConversation(conversationId: String, text: String) {
        conversationMap[conversationId] = text
    }

    fun getConversation(conversationId: String) = conversationMap[conversationId]
}