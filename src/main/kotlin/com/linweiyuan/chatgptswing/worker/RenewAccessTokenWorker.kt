package com.linweiyuan.chatgptswing.worker

import com.linweiyuan.chatgptswing.dataclass.AuthSession
import com.linweiyuan.chatgptswing.extensions.saveAccessToken
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import org.jsoup.Jsoup
import javax.swing.SwingWorker

class RenewAccessTokenWorker(
    private val authSession: AuthSession,
) : SwingWorker<Boolean, Void>() {

    override fun doInBackground() = try {
        val response = Jsoup.connect(Constant.URL_RENEW_ACCESS_TOKEN)
            .ignoreContentType(true)
            .header("Cookie", authSession.cookies)
            .execute()
        response.saveAccessToken()
    } catch (e: Exception) {
        e.toString().warn()
        false
    }

}