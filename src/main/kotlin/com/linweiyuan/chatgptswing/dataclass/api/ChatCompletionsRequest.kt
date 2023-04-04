package com.linweiyuan.chatgptswing.dataclass.api

import com.linweiyuan.chatgptswing.misc.Constant

data class ChatCompletionsRequest(
    val model: String = Constant.MODEL_API,
    val messages: MutableList<Message>,
    val stream: Boolean = true,
)
