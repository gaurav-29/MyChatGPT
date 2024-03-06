package com.abc.mychatgpt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abc.mychatgpt.R
import com.abc.mychatgpt.models.MessageModel

class MessageAdapter(private val messageList: ArrayList<MessageModel>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

//    private val userViewType = 0
//    private val aiViewType = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
//        val view: View
//        if (viewType == userViewType) {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
//        } else {
//            view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_ai, parent, false)
//        }
        return MessageViewHolder(view)
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position])
    }
    override fun getItemCount(): Int {
       return messageList.size
    }

//    override fun getItemViewType(position: Int): Int {
//        val msg: MessageModel = messageList[position]
//        if (msg.isByUser) {
//            return userViewType
//        } else {
//            return aiViewType
//        }
//    }
    class MessageViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
       // var messageTV: TextView = ItemView.findViewById(R.id.userTV)
       private val userTV: TextView = itemView.findViewById(R.id.userTV)
       private val botTV: TextView = itemView.findViewById(R.id.botTV)

        fun bind(messageModel: MessageModel) {
            if (messageModel.isByUser) {
                userTV.visibility = View.VISIBLE
                botTV.visibility = View.GONE
                userTV.text = messageModel.message
            } else {
                userTV.visibility = View.GONE
                botTV.visibility = View.VISIBLE
                botTV.text = messageModel.message
            }
        }
    }
}