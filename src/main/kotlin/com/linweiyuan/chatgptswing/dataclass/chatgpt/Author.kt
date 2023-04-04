package com.linweiyuan.chatgptswing.dataclass.chatgpt

data class Author(
    val role: String,
    val name: String? = null,
    val metadata: Metadata? = null,
)
