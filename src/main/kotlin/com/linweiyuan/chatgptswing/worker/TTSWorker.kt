package com.linweiyuan.chatgptswing.worker

import com.alibaba.fastjson2.JSON
import com.linweiyuan.chatgptswing.dataclass.TTSResponse
import com.linweiyuan.chatgptswing.extensions.warn
import com.linweiyuan.chatgptswing.misc.Constant
import javazoom.jl.player.Player
import org.jsoup.Jsoup
import javax.swing.JButton
import javax.swing.JProgressBar
import javax.swing.SwingWorker

class TTSWorker(
    private val progressBar: JProgressBar,
    private val text: String,
    private val ttsButton: JButton,
) : SwingWorker<TTSResponse, String>() {

    override fun doInBackground(): TTSResponse? {
        updateUI()

        try {
            val json = Jsoup.connect("https://freetts.com/Home/PlayAudio")
                .ignoreContentType(true)
                .data("Language", "en-US")
                .data("Voice", "Kimberly_Female")
                .data("TextMessage", Jsoup.parse(text).text())
                .data("id", "Kimberly")
                .data("type", "1")
                .post()
                .text()
            val ttsResponse = JSON.parseObject(json, TTSResponse::class.java)
            val id = ttsResponse.id
            if (id == null) {
                "Unable to handle this selected texts. (free letters left: ${ttsResponse.counts})".warn()
                return null
            }

            val soundStream = Jsoup.connect("https://freetts.com/audio/$id")
                .ignoreContentType(true)
                .timeout(0)
                .execute()
                .bodyStream()
            Player(soundStream).play()

            return ttsResponse
        } catch (e: Exception) {
            e.toString().warn()
            return null
        }
    }

    override fun done() {
        updateUI()

        val ttsResponse = get()
        if (ttsResponse != null) {
            ttsButton.text = "${Constant.TTS} (${ttsResponse.counts})"
        }
    }

    private fun updateUI() {
        progressBar.isIndeterminate = !progressBar.isIndeterminate
        ttsButton.isEnabled = !ttsButton.isEnabled
    }

}