package com.vktask2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vktask2.data.AppContainer
import com.vktask2.data.Photo
import com.vktask2.data.PicsumRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val items: List<Photo> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isPaging: Boolean = false,
    val error: Boolean = false,
    val pagingError: Boolean = false,
    val endReached: Boolean = false
)

class MainViewModel(
    private val repository: PicsumRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var isRequestRunning = false

    init {
        refresh()
    }

    fun refresh() {
        if (isRequestRunning) return
        currentPage = 0
        _state.value = UiState(isInitialLoading = true)
        loadNextPage(reset = true)
    }

    fun loadMore() {
        loadNextPage(reset = false)
    }

    private fun loadNextPage(reset: Boolean) {
        if (isRequestRunning || _state.value.endReached) return
        isRequestRunning = true

        val targetPage = currentPage + 1
        viewModelScope.launch {
            if (reset) {
                _state.update { it.copy(isInitialLoading = true, error = false, pagingError = false) }
            } else {
                _state.update { it.copy(isPaging = true, pagingError = false) }
            }

            val result = runCatching { repository.loadPage(targetPage, pageSize) }
            result.onSuccess { pageItems ->
                currentPage = targetPage
                val merged = if (reset) {
                    pageItems
                } else {
                    (_state.value.items + pageItems).distinctBy { it.id }
                }
                val reachedEnd = pageItems.size < pageSize
                _state.update {
                    it.copy(
                        items = merged,
                        isInitialLoading = false,
                        isPaging = false,
                        error = false,
                        pagingError = false,
                        endReached = reachedEnd
                    )
                }
            }.onFailure {
                if (reset) {
                    _state.update {
                        it.copy(
                            isInitialLoading = false,
                            error = true,
                            isPaging = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isPaging = false,
                            pagingError = true
                        )
                    }
                }
            }
            isRequestRunning = false
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(appContainer.repository)
            }
        }
    }
}

