package com.linweiyuan.chatgptswing.dataclass.api

import com.alibaba.fastjson2.annotation.JSONField

data class Choice(
    val delta: Delta,
    @JSONField(name = "finish_reason")
    val finishReason: String?
)