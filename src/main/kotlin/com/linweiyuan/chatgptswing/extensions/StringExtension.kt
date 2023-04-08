package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.MainFrame
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

fun String.warn(mainFrame: MainFrame?) = SwingUtilities.invokeLater {
    JOptionPane.showMessageDialog(mainFrame, this, "Warning", JOptionPane.WARNING_MESSAGE)
}
