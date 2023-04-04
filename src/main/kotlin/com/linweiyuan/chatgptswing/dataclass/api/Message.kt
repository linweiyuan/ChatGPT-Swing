package com.linweiyuan.chatgptswing.dataclass.api

import com.linweiyuan.chatgptswing.misc.Constant

data class Message(
    val role: String = Constant.ROLE_USER,
    val content: String,
)
