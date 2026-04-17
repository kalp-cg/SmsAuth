package com.kalpcg.pulserelay.modules.incoming.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessage
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessageType
import com.kalpcg.pulserelay.modules.incoming.db.IncomingMessageTotals
import com.kalpcg.pulserelay.modules.incoming.repositories.IncomingMessagesRepository
import java.util.Locale

class IncomingMessagesListViewModel(
    private val repository: IncomingMessagesRepository,
) : ViewModel() {
    val totals: LiveData<IncomingMessageTotals> = repository.totals

    private val limit = MutableLiveData(chunkSize)
    private val allMessages = MutableLiveData<List<IncomingMessage>>(emptyList())
    private val searchQuery = MutableLiveData("")
    private val typeFilter = MutableLiveData(TypeFilter.ALL)
    private val _messages = MediatorLiveData<List<IncomingMessage>>()
    val messages: LiveData<List<IncomingMessage>> = _messages

    private var isLoading = false
    private var hasMore = true

    init {
        _messages.addSource(limit.switchMap { repository.selectLast(it) }) {
            allMessages.value = it
            hasMore = it.size >= (limit.value ?: chunkSize)
            isLoading = false
            refreshFilteredMessages()
        }
        _messages.addSource(searchQuery) { refreshFilteredMessages() }
        _messages.addSource(typeFilter) { refreshFilteredMessages() }
    }

    fun loadMore(index: Int = 0) {
        val currentLimit = limit.value ?: 0
        if (currentLimit >= index + chunkSize || isLoading || !hasMore) return

        isLoading = true
        limit.value = currentLimit + chunkSize
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setTypeFilter(filter: TypeFilter) {
        typeFilter.value = filter
    }

    private fun refreshFilteredMessages() {
        val source = allMessages.value.orEmpty()
        val query = searchQuery.value.orEmpty().trim().lowercase(Locale.getDefault())
        val messageTypeFilter = typeFilter.value ?: TypeFilter.ALL

        _messages.value = source.filter { message ->
            val matchesType = when (messageTypeFilter) {
                TypeFilter.ALL -> true
                TypeFilter.SMS -> message.type == IncomingMessageType.SMS
                TypeFilter.DATA_SMS -> message.type == IncomingMessageType.DATA_SMS
                TypeFilter.MMS ->
                    message.type == IncomingMessageType.MMS || message.type == IncomingMessageType.MMS_DOWNLOADED
            }

            if (!matchesType) {
                return@filter false
            }

            if (query.isBlank()) {
                return@filter true
            }

            message.sender.lowercase(Locale.getDefault()).contains(query) ||
                    message.contentPreview.lowercase(Locale.getDefault()).contains(query) ||
                    (message.recipient?.lowercase(Locale.getDefault())?.contains(query) == true)
        }
    }

    enum class TypeFilter {
        ALL,
        SMS,
        DATA_SMS,
        MMS,
    }

    companion object {
        private const val chunkSize = 50
    }
}
