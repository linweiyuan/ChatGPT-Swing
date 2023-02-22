package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class ChatRequest(
    val action: String = "next",
    val model: String = "text-davinci-002-render-sha",
    @JSONField(name = "parent_message_id")
    val parentMessageId: String,
    val messages: List<Message>,
)

data class Message(
    val id: String,
    val author: Author,
    val content: Content,
)

data class Author(
    val role: String,
)

data class Content(
    @JSONField(name = "content_type")
    val contentType: String = "text",
    val parts: MutableList<String>,
)

data class ChatResponse(
    @JSONField(name = "conversation_id")
    val conversationId: String,
    val message: Message,
    val error: String?
)