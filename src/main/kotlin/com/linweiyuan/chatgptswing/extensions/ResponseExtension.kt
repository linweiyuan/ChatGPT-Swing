package com.linweiyuan.chatgptswing.extensions

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection
import java.io.File

fun Connection.Response.showErrorMessage() {
    JSON.parseObject(body()).getString("errorMessage").warn()
}

fun Connection.Response.saveAccessToken(): Boolean {
    if (statusCode() != Constant.HTTP_OK) {
        showErrorMessage()
        return false
    }

    val authSession = JSON.parseObject(body(), AuthSession::class.java)
    File(System.getProperty("user.home"), Constant.AUTH_SESSION_FILE_NAME).writeText(
        JSON.toJSONString(authSession, JSONWriter.Feature.PrettyFormat)
    )
    return true
}