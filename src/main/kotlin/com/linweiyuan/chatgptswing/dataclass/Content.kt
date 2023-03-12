package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class Content(
    @JSONField(name = "content_type")
    val contentType: String,
    val parts: List<String>,
)
