package com.linweiyuan.chatgptswing.dataclass.api

import com.alibaba.fastjson2.annotation.JSONField

data class Usage(
    @JSONField(name = "total_granted")
    val totalGranted: Double,
    @JSONField(name = "total_used")
    val totalUsed: Double,
    @JSONField(name = "total_available")
    val totalAvailable: Double,
)
