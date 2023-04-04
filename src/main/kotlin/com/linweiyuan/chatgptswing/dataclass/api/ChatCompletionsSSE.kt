package com.linweiyuan.chatgptswing.dataclass.api

data class ChatCompletionsSSE(
    val id: String,
    val choices: List<Choice>
)
