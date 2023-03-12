package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.ConfigUtil
import org.jsoup.Connection

fun Connection.preset(): Connection = this
    .ignoreContentType(true)
    .ignoreHttpErrors(true)
    .timeout(Constant.TIMEOUT_SECONDS * 1000)
    .header(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON)
    .header(Constant.AUTHORIZATION, ConfigUtil.getAccessToken())
