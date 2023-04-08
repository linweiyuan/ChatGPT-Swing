package com.linweiyuan.chatgptswing.worker.api

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.MainFrame
import com.linweiyuan.chatgptswing.dataclass.api.Usage
import com.linweiyuan.chatgptswing.extensions.preset
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import com.linweiyuan.chatgptswing.util.ConfigUtil
import org.jsoup.Jsoup
import javax.swing.JOptionPane
import javax.swing.SwingWorker

class CheckUsageWorker(
    private val mainFrame: MainFrame,
) : SwingWorker<Usage, Void>() {
    override fun doInBackground(): Usage? {
        return try {
            val url = "${ConfigUtil.getServerUrl()}${Constant.URL_API_CHECK_USAGE}"
            val json = Jsoup
                .connect(url)
                .preset(false)
                .get()
                .text()
            JSON.parseObject(json, Usage::class.java)
        } catch (e: Exception) {
            e.toString().warn(mainFrame)
            null
        }
    }

    override fun done() {
        mainFrame.progressBar.isIndeterminate = false

        val usage = get()
        if (usage != null) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Total Granted: ${usage.totalGranted}\n" +
                        "Total Used: ${usage.totalUsed}\n" +
                        "Total Available: ${usage.totalAvailable}"
            )
        }
    }
}
