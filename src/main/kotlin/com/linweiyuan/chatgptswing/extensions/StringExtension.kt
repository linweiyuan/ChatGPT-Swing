package com.linweiyuan.chatgptswing.extensions

import javax.swing.JOptionPane
import javax.swing.SwingUtilities

fun String.warn() = SwingUtilities.invokeLater {
    JOptionPane.showMessageDialog(null, this, "Warning", JOptionPane.WARNING_MESSAGE)
}
