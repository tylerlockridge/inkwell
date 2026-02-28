package com.obsidiancapture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.data.repository.InboxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val inboxRepository: InboxRepository,
) : ViewModel() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun triggerStartupSync() {
        viewModelScope.launch {
            inboxRepository.syncInbox()
        }
    }
}
