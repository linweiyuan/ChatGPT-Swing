@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField
import java.util.*

data class Message(
    val id: String,
    val author: Author,
    val content: Content,
) {
    override fun toString() = content.parts[0].trim()
}

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