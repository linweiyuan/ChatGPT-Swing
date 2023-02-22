package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection

fun Connection.useDefault(accessToken: String = ""): Connection {
    var connection = this.ignoreContentType(true)
        .ignoreHttpErrors(true)
        .userAgent(Constant.USER_AGENT)
    if (accessToken.isNotBlank()) {
        connection = connection.headers(
            mapOf(
                Constant.CONTENT_TYPE to Constant.APPLICATION_JSON,
                Constant.AUTHORIZATION to "${Constant.BEARER} $accessToken",
            )
        )
    }
    return connection
}