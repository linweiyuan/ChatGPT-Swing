package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class ConversationContent(
    @JSONField(name = "create_time")
    val createTime: Long,
    @JSONField(name = "current_node")
    val currentNode: String,
    val mapping: Map<String, ConversationMapping>,
    @JSONField(name = "moderation_results")
    val moderationResults: List<String>,
    val title: String,
)
