package com.linweiyuan.chatgptswing.dataclass

import com.alibaba.fastjson2.annotation.JSONField

data class Metadata(
    @JSONField(name = "message_type")
    val messageType: String?,
    @JSONField(name = "model_slug")
    val modelSlug: String?,
)
