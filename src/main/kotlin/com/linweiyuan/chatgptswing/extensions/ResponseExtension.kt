package com.linweiyuan.chatgptswing.extensions

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import org.jsoup.Connection

fun Connection.Response.showErrorMessage(mainFrame: MainFrame) {
    JSON.parseObject(body()).getString("detail").warn(mainFrame)
}
