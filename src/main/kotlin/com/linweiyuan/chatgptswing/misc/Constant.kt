@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.misc

object Constant {
    const val TITLE = "ChatGPT-Swing"

    const val AUTH_SESSION_FILE_NAME = ".chatgpt-swing.json"

    const val USERNAME = "Email"
    const val PASSWORD = "Password"
    const val LOGIN = "Login"
    const val CONTENT = "Content"
    const val REFRESH = "Refresh"

    const val MAGIC_NUMBER = 1

    const val LOGIN_FIELD_WIDTH = 30

    const val USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"

    const val HTTP_OK = 200
    const val HTTP_BAD_REQUEST = 400
    const val HTTP_TOO_MANY_REQUESTS = 429
    const val HTTP_INTERNAL_SERVER_ERROR = 500

    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val BEARER = "Bearer"
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_HTML = "text/html"

    const val DEFAULT_NEW_CONVERSATION_DISPLAY_TEXT = "+ New chat"

    const val CONVERSATION_LIST_FETCH_COUNT = 100 // max 100

    const val ROLE_USER = "user"
    const val MODEL_NAME = "text-davinci-002-render-sha"
}