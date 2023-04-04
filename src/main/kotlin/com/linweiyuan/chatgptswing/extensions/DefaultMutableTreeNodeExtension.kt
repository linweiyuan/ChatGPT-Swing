package com.linweiyuan.chatgptswing.extensions

import com.linweiyuan.chatgptswing.dataclass.chatgpt.Conversation
import javax.swing.tree.DefaultMutableTreeNode

fun DefaultMutableTreeNode.getCurrentNode(conversationId: String) = children().toList().find {
    val node = it as DefaultMutableTreeNode
    if (node.parent == this) {
        val conversation = node.userObject as Conversation
        conversation.id == conversationId
    } else {
        false
    }
}?.let {
    it as DefaultMutableTreeNode
}
