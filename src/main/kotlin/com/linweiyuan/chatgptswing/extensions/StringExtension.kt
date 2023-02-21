package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.SingletonUtil
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

fun String.warn() = SwingUtilities.invokeLater {
    JOptionPane.showMessageDialog(null, this, "Warning", JOptionPane.WARNING_MESSAGE)
}

fun String.toHtml(): String = SingletonUtil.getHtmlRenderer().render(SingletonUtil.getParser().parse(this))