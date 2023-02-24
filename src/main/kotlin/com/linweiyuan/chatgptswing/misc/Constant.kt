@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.misc

object Constant {
    const val TITLE = "ChatGPT-Swing"

    const val DEFAULT_WIDTH = 1366
    const val DEFAULT_HEIGHT = 768

    const val AUTH_SESSION_FILE_NAME = ".chatgpt-swing.json"

    const val TOKEN_RENEW_BEFORE_EXPIRATION_DAYS = 1L

    private const val SERVER_ADDRESS = "https://api.linweiyuan.com/chatgpt"

    const val URL_LOGIN = "$SERVER_ADDRESS/user/login"
    const val URL_RENEW_ACCESS_TOKEN = "$SERVER_ADDRESS/auth/session"
    const val URL_GET_CONVERSATION_LIST = "$SERVER_ADDRESS/conversations"
    const val URL_GET_CONVERSATION_CONTENT = "$SERVER_ADDRESS/conversation/%s"
    const val URL_MAKE_CONVERSATION = "$SERVER_ADDRESS/conversation"
    const val URL_CLEAR_ALL_CONVERSATIONS = "$SERVER_ADDRESS/conversations"
    const val URL_DELETE_CONVERSATION = "$SERVER_ADDRESS/conversation/%s"
    const val URL_RENAME_CONVERSATION = "$SERVER_ADDRESS/conversation/%s"
    const val URL_GEN_CONVERSATION_TITLE = "$SERVER_ADDRESS/conversation/gen_title/%s"
    const val URL_ADD_MESSAGE_FEEDBACK = "$SERVER_ADDRESS/conversation/message_feedback"

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
    const val FEEDBACK = "Feedback"
    const val FEEDBACK_LIKE = "Like"
    const val FEEDBACK_CANCEL = "Cancel"
    const val FEEDBACK_DISLIKE = "Dislike"
    const val FEEDBACK_THUMBS_UP = "thumbsUp"
    const val FEEDBACK_THUMBS_DOWN = "thumbsDown"
    const val TTS = "TTS"
    const val ABOUT = "About"
    const val GITHUB_REPO_URL = "https://github.com/linweiyuan/ChatGPT-Swing"
    const val ABOUT_INTO = "GitHub: $GITHUB_REPO_URL (click to open)"
    const val MORE = "More"
    const val RENAME = "Rename"
    const val NEW = "New"
    const val DELETE = "Delete"
    const val CLEAR = "Clear"

    const val MAGIC_NUMBER = 1

    const val LOGIN_FIELD_WIDTH = 30
    const val SPLIT_PANE_DIVIDER_LOCATION = 235

    const val TIMEOUT_SECONDS = 123

    const val HTTP_OK = 200

    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_HTML = "text/html"

    const val DEFAULT_NEW_CONVERSATION_DISPLAY_TEXT = "+ New chat"

    const val ROLE_USER = "user"

    const val HTML_NEW_LINE = "<br />"

    const val GREETING_CHINESE = "你好，我是%s，请问你叫什么名字？"
    const val GREETING_ENGLITH = "Hello, I am %s, may I have your name?"
}