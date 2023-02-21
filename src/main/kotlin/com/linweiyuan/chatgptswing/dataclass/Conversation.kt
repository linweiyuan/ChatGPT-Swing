package com.linweiyuan.chatgptswing.dataclass

data class Conversation(
    val id: String,
    val title: String
) {
    override fun toString() = title
}

data class ConversationListResponse(
    val items: List<Conversation>,
    val total: Int,
    val limit: Int,
    val offset: Int
)