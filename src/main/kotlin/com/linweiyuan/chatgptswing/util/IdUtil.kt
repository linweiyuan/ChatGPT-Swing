package com.linweiyuan.chatgptswing.util

object IdUtil {
    private var conversationId = ""
    private var parentMessageId = ""

    fun setConversationId(conversationId: String) {
        this.conversationId = conversationId
    }

    fun getConversationId() = conversationId

    fun setParentMessageId(parentMessageId: String) {
        this.parentMessageId = parentMessageId
    }

    fun getParentMessageId() = parentMessageId

    fun clearIds() {
        conversationId = ""
        parentMessageId = ""
    }
}
