package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class Conversation(
    val id: String,
    val title: String,
) {
    override fun toString() = title.trim()
}

data class ConversationListResponse(
    val items: List<Conversation>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)

data class ConversationContentResponse(
    @JSONField(name = "current_node")
    val currentNode: String,
    val mapping: Map<String, ConversationDetail>,
    val title: String,
)

data class ConversationDetail(
    val id: String,
    val message: Message?,
    val parent: String?,
    val children: List<String>,
)

data class GenerateTitleResponse(
    val title: String,
)