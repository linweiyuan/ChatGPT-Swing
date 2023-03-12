package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class ConversationSSE(
    val message: Message,
    @JSONField(name = "conversation_id")
    val conversationId: String,
    val error: String?,
)
