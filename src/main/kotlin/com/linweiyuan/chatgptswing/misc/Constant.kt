@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.misc

object Constant {
    const val TITLE = "ChatGPT-Swing"

    const val AUTH_SESSION_FILE_NAME = ".chatgpt-swing.json"

    const val USERNAME = "Email"
    const val PASSWORD = "Password"
    const val PROXY_HOST = "Proxy Host"
    const val PROXY_PORT = "Proxy Port"
    const val PROXY_TYPE_NONE = "NONE"
    const val PROXY_TYPE_HTTP = "HTTP"
    const val PROXY_TYPE_SOCKS5 = "SOCKS5"
    const val LOGIN = "Login"
    const val CONTENT = "Content"
    const val REFRESH = "Refresh"
    const val TTS = "TTS"
    const val ABOUT = "About"
    const val GITHUB_REPO_URL = "https://github.com/linweiyuan/ChatGPT-Swing"
    const val ABOUT_INTO = "GitHub: $GITHUB_REPO_URL (click to open)"
    const val MORE = "More"

    const val MAGIC_NUMBER = 1

    const val LOGIN_FIELD_WIDTH = 30

    const val USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"

    const val TIMEOUT_SECONDS = 123

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

    const val DIV_BACKGROUND_COLOR_PREFIX_USER = "<div style='background-color: #343541; color: F7F7F8'>"
    const val DIV_POSTFIX = "</div>"
    const val DIV_BACKGROUND_COLOR_PREFIX_ASSISTANT = "<div style='background-color: #F7F7CB; color: #374151'>"
    const val HTML_NEW_LINE = "<br />"
}