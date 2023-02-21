package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Connection

fun Connection.useDefault(): Connection = this
    .ignoreContentType(true)
    .ignoreHttpErrors(true)
    .timeout(0)
    .userAgent(Constant.USER_AGENT)