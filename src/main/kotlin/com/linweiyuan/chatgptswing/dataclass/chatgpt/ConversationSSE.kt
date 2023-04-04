package com.linweiyuan.chatgptswing.dataclass.chatgpt

import com.alibaba.fastjson2.annotation.JSONField

data class ConversationSSE(
    val message: Message,
    @JSONField(name = "conversation_id")
    val conversationId: String,
    val error: String?,
)
