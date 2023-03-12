package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.misc.Constant
import java.awt.GridBagConstraints
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.text.JTextComponent

fun JTextComponent.wrapped(title: String) = JPanel().apply {
    border = BorderFactory.createTitledBorder(title)
    layout = GridLayout()

    with(Constant.MAGIC_NUMBER) {
        add(
            this@wrapped, GridBagConstraints(
                this,
                this,
                this,
                this,
                this.toDouble(),
                this.toDouble(),
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                Insets(this, this, this, this),
                this,
                this
            ).apply {
                anchor = GridBagConstraints.EAST
            }
        )
    }
}
