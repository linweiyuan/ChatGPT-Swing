package com.linweiyuan.chatgptswing.dataclass

data class AuthSession(
    val user: User,
    val expires: String,
    val accessToken: String
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val image: String,
    val picture: String,
    val groups: List<String>,
)