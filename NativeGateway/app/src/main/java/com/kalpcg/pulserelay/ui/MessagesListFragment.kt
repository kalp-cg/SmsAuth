package com.kalpcg.pulserelay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kalpcg.pulserelay.R
import com.kalpcg.pulserelay.data.entities.Message
import com.kalpcg.pulserelay.databinding.FragmentMessagesListBinding
import com.kalpcg.pulserelay.modules.messages.vm.MessagesListViewModel
import com.kalpcg.pulserelay.ui.adapters.MessagesAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class MessagesListFragment : Fragment(), MessagesAdapter.OnItemClickListener<Message> {

    private val viewModel: MessagesListViewModel by viewModel()
    private val messagesAdapter = MessagesAdapter(this)
    private var _binding: FragmentMessagesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMessagesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = messagesAdapter
        binding.recyclerView.addOnScrollListener(scrollListener)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        // Observe stats LiveData
        viewModel.totals.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.totalMessages.text = getString(R.string.total_messages, it.total)
                binding.pendingMessages.text = getString(R.string.pending_messages, it.pending)
                binding.sentMessages.text = getString(R.string.sent_messages, it.sent)
                binding.deliveredMessages.text =
                    getString(R.string.delivered_messages, it.delivered)
                binding.failedMessages.text = getString(R.string.failed_messages, it.failed)
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            val shouldScrollToTop = _binding?.recyclerView?.computeVerticalScrollOffset() == 0
            messagesAdapter.submitList(it) {
                if (shouldScrollToTop) _binding?.recyclerView?.scrollToPosition(0)
            }
        }

        binding.searchMessagesInput.doAfterTextChanged {
            viewModel.setSearchQuery(it?.toString().orEmpty())
        }

        binding.stateFilterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedId = checkedIds.firstOrNull() ?: R.id.filterStateAll
            val filter = when (selectedId) {
                R.id.filterStatePending -> MessagesListViewModel.StateFilter.PENDING
                R.id.filterStateSent -> MessagesListViewModel.StateFilter.SENT
                R.id.filterStateDelivered -> MessagesListViewModel.StateFilter.DELIVERED
                R.id.filterStateFailed -> MessagesListViewModel.StateFilter.FAILED
                else -> MessagesListViewModel.StateFilter.ALL
            }
            viewModel.setStateFilter(filter)
        }
    }

    override fun onItemClick(item: Message) {
        parentFragmentManager.commit {
            replace(R.id.rootLayout, MessageDetailsFragment.newInstance(item.id))
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.removeOnScrollListener(scrollListener)
        super.onDestroyView()
        _binding = null
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val linearLayoutManager = recyclerView.layoutManager as? LinearLayoutManager
            linearLayoutManager?.findLastVisibleItemPosition()?.let {
                if (it == messagesAdapter.itemCount - 1) viewModel.loadMore(messagesAdapter.itemCount)
            }
        }
    }

    companion object {
        fun newInstance() =
            MessagesListFragment()
    }
}