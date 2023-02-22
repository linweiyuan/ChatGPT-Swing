package com.linweiyuan.chatgptswing.listmodel

import com.linweiyuan.chatgptswing.dataclass.Conversation
import com.linweiyuan.chatgptswing.misc.Constant
import javax.swing.AbstractListModel

class ConversationListModel(private val conversations: MutableList<Conversation>) : AbstractListModel<Conversation>() {

    override fun getSize() = conversations.size

    override fun getElementAt(index: Int) = conversations[index]

    fun addItem(conversation: Conversation) {
        conversations.add(conversation)
    }

    fun done() {
        fireContentsChanged(this, 0, size - 1)
    }

    fun clear() {
        conversations.clear()
        conversations.add(Conversation("", Constant.DEFAULT_NEW_CONVERSATION_DISPLAY_TEXT))
    }

    fun getIndexByConversationId(conversationId: String): Int {
        conversations.forEachIndexed { index, conversation ->
            if (conversation.id == conversationId) {
                return index
            }
        }
        return 0
    }

}