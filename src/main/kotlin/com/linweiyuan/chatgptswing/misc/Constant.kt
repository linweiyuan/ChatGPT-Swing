@file:Suppress("SpellCheckingInspection")

package com.linweiyuan.chatgptswing.misc

object Constant {
    const val TITLE = "ChatGPT-Swing"

    const val DEFAULT_WIDTH = 1366
    const val DEFAULT_HEIGHT = 768

    const val CONFIG_FILE_NAME = ".chatgpt-swing.json"

    const val URL_START_CONVERSATION = "/conversation"
    const val URL_GET_CONVERSATION_LIST = "/conversations"
    const val URL_GET_CONVERSATION_CONTENT = "/conversation/%s"
    const val URL_CLEAR_ALL_CONVERSATIONS = "/conversations"
    const val URL_DELETE_CONVERSATION = "/conversation/%s"
    const val URL_RENAME_TITLE = "/conversation/%s"
    const val URL_GENERATE_TITLE = "/conversation/gen_title/%s"
    const val URL_ADD_MESSAGE_FEEDBACK = "/conversation/message_feedback"

    const val MODEL = "text-davinci-002-render-sha"

    const val CONTENT = "Content"
    const val REFRESH = "Refresh"
    const val FEEDBACK = "Feedback"
    const val FEEDBACK_LIKE = "Like"
    const val FEEDBACK_CANCEL = "Cancel"
    const val FEEDBACK_DISLIKE = "Dislike"
    const val FEEDBACK_THUMBS_UP = "thumbsUp"
    const val FEEDBACK_THUMBS_DOWN = "thumbsDown"
    const val TTS = "TTS"
    const val CONFIG = "Config"
    const val ABOUT = "About"
    const val GITHUB_REPO_URL = "https://github.com/linweiyuan/ChatGPT-Swing"
    const val ABOUT_INTO = "GitHub: $GITHUB_REPO_URL (click to open)"
    const val MORE = "More"
    const val RENAME = "Rename"
    const val NEW = "New"
    const val DELETE = "Delete"
    const val CLEAR = "Clear"

    const val MAGIC_NUMBER = 1

    const val SPLIT_PANE_DIVIDER_LOCATION = 235

    const val TIMEOUT_SECONDS = 123

    const val HTTP_OK = 200

    const val CONTENT_TYPE = "Content-Type"
    const val APPLICATION_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"

    const val MESSAGE_CONTENT_TYPE_TEXT = "text"
    const val ROLE_USER = "user"
}
