package com.linweiyuan.chatgptswing.extensions

import com.alibaba.fastjson2.JSON
import org.jsoup.Connection

fun Connection.Response.showErrorMessage() {
    JSON.parseObject(body()).getString("errorMessage").warn()
}