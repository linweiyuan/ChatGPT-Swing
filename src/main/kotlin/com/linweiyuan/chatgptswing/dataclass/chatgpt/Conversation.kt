package com.linweiyuan.chatgptswing.dataclass.chatgpt

import com.alibaba.fastjson2.annotation.JSONField

data class Conversation(
    @JSONField(name = "create_time")
    val createTime: String,
    val id: String,
    val title: String,
) {
    override fun toString() = title.trim()
}
