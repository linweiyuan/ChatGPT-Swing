package com.linweiyuan.chatgptswing.misc

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object SingletonUtil {
    private val _htmlRenderer = HtmlRenderer.builder().build()
    private val _parser = Parser.builder().build()

    fun getHtmlRenderer(): HtmlRenderer = _htmlRenderer

    fun getParser(): Parser = _parser
}