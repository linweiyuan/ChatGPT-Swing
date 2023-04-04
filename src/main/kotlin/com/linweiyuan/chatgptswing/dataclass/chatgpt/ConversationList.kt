package com.linweiyuan.chatgptswing.dataclass.chatgpt

data class ConversationList(
    val items: List<Conversation>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)
