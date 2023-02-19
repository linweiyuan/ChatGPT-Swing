package com.linweiyuan.chatgptswing

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

class MainFrame : JFrame() {
    init {
        title = "Testing"
        size = Dimension(300, 200)
        setLocationRelativeTo(null)
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    SwingUtilities.invokeLater { MainFrame() }
}
