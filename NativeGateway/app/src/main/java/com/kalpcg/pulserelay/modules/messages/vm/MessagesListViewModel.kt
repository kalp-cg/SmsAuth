package com.kalpcg.pulserelay.modules.messages.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.kalpcg.pulserelay.data.entities.Message
import com.kalpcg.pulserelay.data.entities.MessagesTotals
import com.kalpcg.pulserelay.domain.ProcessingState
import com.kalpcg.pulserelay.modules.messages.MessagesRepository
import java.util.Locale

class MessagesListViewModel(
    private val messagesRepo: MessagesRepository
) : ViewModel() {
    val totals: LiveData<MessagesTotals> =
        messagesRepo.messagesTotals

    private val _limit = MutableLiveData<Int>(chunkSize)
    private val _allMessages = MutableLiveData<List<Message>>(emptyList())
    private val _searchQuery = MutableLiveData("")
    private val _stateFilter = MutableLiveData(StateFilter.ALL)
    private val _messages = MediatorLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    init {
        _messages.addSource(_limit.switchMap { messagesRepo.selectLast(it) }) {
            _allMessages.value = it
            hasMore = it.size >= (_limit.value ?: chunkSize)
            isLoading = false
            refreshFilteredMessages()
        }
        _messages.addSource(_searchQuery) { refreshFilteredMessages() }
        _messages.addSource(_stateFilter) { refreshFilteredMessages() }
        loadMore()
    }

    private var isLoading = false
    private var hasMore = true

    fun loadMore(index: Int = 0) {
        val currentLimit = _limit.value ?: 0
        if (currentLimit >= index + chunkSize || isLoading || !hasMore) return

        isLoading = true
        _limit.value = currentLimit + chunkSize
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStateFilter(filter: StateFilter) {
        _stateFilter.value = filter
    }

    private fun refreshFilteredMessages() {
        val source = _allMessages.value.orEmpty()
        val query = _searchQuery.value.orEmpty().trim().lowercase(Locale.getDefault())
        val stateFilter = _stateFilter.value ?: StateFilter.ALL

        _messages.value = source.filter { message ->
            val matchesState = when (stateFilter) {
                StateFilter.ALL -> true
                StateFilter.PENDING -> message.state == ProcessingState.Pending
                StateFilter.SENT -> message.state == ProcessingState.Sent
                StateFilter.DELIVERED -> message.state == ProcessingState.Delivered
                StateFilter.FAILED -> message.state == ProcessingState.Failed
            }

            if (!matchesState) {
                return@filter false
            }

            if (query.isBlank()) {
                return@filter true
            }

            message.id.lowercase(Locale.getDefault()).contains(query) ||
                    message.content.lowercase(Locale.getDefault()).contains(query) ||
                    message.state.name.lowercase(Locale.getDefault()).contains(query)
        }
    }

    enum class StateFilter {
        ALL,
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
    }

    companion object {
        private const val chunkSize = 50
    }
}