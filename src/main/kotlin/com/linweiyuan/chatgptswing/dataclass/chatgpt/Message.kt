package com.linweiyuan.chatgptswing.dataclass.chatgpt

data class Message(
    val id: String,
    val author: Author,
    val createTime: Long? = null,
    val updateTime: Long? = null,
    val content: Content,
    val endTurn: Boolean? = null,
//    val weight: Double,
//    val metadata: Metadata,
//    val recipient: String,
) {
    override fun toString() = content.parts[0].trim()
}
