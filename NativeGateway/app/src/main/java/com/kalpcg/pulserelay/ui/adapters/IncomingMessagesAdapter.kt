package com.kalpcg.pulserelay.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kalpcg.pulserelay.R
import com.kalpcg.pulserelay.databinding.ItemIncomingMessageBinding
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessage
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessageType
import java.text.DateFormat
import java.util.Date

class IncomingMessagesAdapter :
    ListAdapter<IncomingMessage, IncomingMessagesAdapter.IncomingMessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingMessageViewHolder {
        return IncomingMessageViewHolder(
            ItemIncomingMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IncomingMessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncomingMessageViewHolder(
        private val binding: ItemIncomingMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IncomingMessage) {
            binding.textViewSender.text = item.sender
            binding.textViewType.text = when (item.type) {
                IncomingMessageType.SMS -> binding.root.context.getString(R.string.incoming_type_sms)
                IncomingMessageType.DATA_SMS -> binding.root.context.getString(R.string.incoming_type_data_sms)
                IncomingMessageType.MMS,
                IncomingMessageType.MMS_DOWNLOADED -> binding.root.context.getString(R.string.incoming_type_mms)
            }
            binding.textViewDate.text =
                DateFormat.getDateTimeInstance().format(Date(item.createdAt))
            val maskedPreview = item.contentPreview.replace(Regex("\\b\\d{6}\\b"), "******")
            binding.textViewPreview.text = maskedPreview
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<IncomingMessage>() {
        override fun areItemsTheSame(oldItem: IncomingMessage, newItem: IncomingMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: IncomingMessage,
            newItem: IncomingMessage
        ): Boolean {
            return oldItem == newItem
        }
    }
}
