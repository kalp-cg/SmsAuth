package com.kalpcg.pulserelay.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kalpcg.pulserelay.R
import com.kalpcg.pulserelay.databinding.ItemLogEntryBinding
import com.kalpcg.pulserelay.modules.logs.db.LogEntry
import java.text.DateFormat
import java.util.Date

class LogItemsAdapter(
) : ListAdapter<LogEntry, LogItemsAdapter.ViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemLogEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLogEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogEntry) {
            binding.textViewDate.text =
                DateFormat.getDateTimeInstance().format(Date(item.createdAt))
            val maskedMessage = item.message.replace(Regex("\\b\\d{6}\\b"), "******")
            binding.textViewMessage.text = maskedMessage
            binding.textViewModule.text = item.module

            binding.imageViewPriority.setImageResource(
                when (item.priority) {
                    LogEntry.Priority.DEBUG -> R.drawable.ic_log_debug
                    LogEntry.Priority.INFO -> R.drawable.ic_log_info
                    LogEntry.Priority.WARN -> R.drawable.ic_log_warn
                    LogEntry.Priority.ERROR -> R.drawable.ic_log_error
                }
            )
        }
    }

    class LogDiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LogEntry,
            newItem: LogEntry
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

}