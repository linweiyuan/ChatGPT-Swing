package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.MainFrame
import org.jsoup.Connection

fun Connection.Response.showErrorMessage(mainFrame: MainFrame) {
    body().warn(mainFrame)
}
