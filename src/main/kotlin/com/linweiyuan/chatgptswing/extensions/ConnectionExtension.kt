package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection

fun Connection.useDefault(accessToken: String = ""): Connection {
    var connection = this.ignoreContentType(true)
        .ignoreHttpErrors(true)
        .timeout(Constant.TIMEOUT_SECONDS * 1000)
        .header(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON)
    if (accessToken.isNotBlank()) {
        connection = connection.headers(
            mapOf(
                Constant.AUTHORIZATION to accessToken,
            )
        )
    }
    return connection
}