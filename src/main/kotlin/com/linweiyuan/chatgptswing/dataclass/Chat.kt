@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField
import com.linweiyuan.chatgptswing.misc.Constant
import java.util.*

data class ChatRequest(
    val action: String = "next",
    val model: String = Constant.MODEL_NAME,
    @JSONField(name = "parent_message_id")
    var parentMessageId: String = UUID.randomUUID().toString(),
    val messages: List<Message>,
    @JSONField(name = "conversation_id")
    var conversationId: String? = null,
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