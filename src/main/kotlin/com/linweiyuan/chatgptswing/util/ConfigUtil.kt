package com.linweiyuan.chatgptswing.util

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Config
import com.linweiyuan.chatgptswing.misc.Constant
import java.io.File

object ConfigUtil {
    private var serverUrl = ""
    private var accessToken = ""

    private val configFile = File(System.getProperty("user.home"), Constant.CONFIG_FILE_NAME)

    init {
        if (!configFile.exists()) {
            configFile.createNewFile()
        } else {
            val configJson = configFile.readText()
            if (configJson.isBlank()) {
                "Please set server url and access token in menu first at top left.".warn()
            } else {
                val config = JSON.parseObject(configJson, Config::class.java)
                serverUrl = config.serverUrl
                accessToken = config.accessToken
            }
        }
    }

    fun getServerUrl() = serverUrl

    fun getAccessToken(): String = accessToken

    fun saveConfig(serverUrl: String, accessToken: String) {
        this.serverUrl = serverUrl
        this.accessToken = accessToken

        File(System.getProperty("user.home"), Constant.CONFIG_FILE_NAME).writeText(
            JSON.toJSONString(Config(serverUrl, accessToken), JSONWriter.Feature.PrettyFormat)
        )
    }
}
