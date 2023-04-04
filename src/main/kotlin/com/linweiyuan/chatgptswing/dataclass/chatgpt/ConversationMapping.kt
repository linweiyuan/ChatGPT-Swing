package com.linweiyuan.chatgptswing.dataclass.chatgpt

data class ConversationMapping(
    val id: String,
    val message: Message?,
    val parent: String?,
    val children: List<String>,
)
