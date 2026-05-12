package com.example.janaushadhifinder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.janaushadhifinder.data.MedicineRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MedicineSearchUiState(
    val query: String = "",
    val medicines: List<Medicine> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val closestMatch: String? = null,
    val isLoading: Boolean = true,
    val dataSource: String = "Room"
)

private data class SearchData(
    val medicines: List<Medicine> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val closestMatch: String? = null
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MedicineSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicineRepository(application)
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(true)
    private val dataSource = MutableStateFlow("Room")

    private val searchData = query
        .debounce(180)
        .distinctUntilChanged()
        .flatMapLatest { debouncedQuery ->
            combine(
                repository.observeMedicines(debouncedQuery),
                repository.observeSuggestions(debouncedQuery),
                repository.observeClosestMatch(debouncedQuery)
            ) { medicines, suggestions, closestMatch ->
                SearchData(
                    medicines = medicines,
                    suggestions = suggestions,
                    closestMatch = closestMatch
                )
            }
        }

    val uiState: StateFlow<MedicineSearchUiState> = combine(
        query,
        searchData,
        loading,
        dataSource
    ) { currentQuery, search, isLoading, source ->
        MedicineSearchUiState(
            query = currentQuery,
            medicines = search.medicines,
            suggestions = search.suggestions,
            closestMatch = search.closestMatch,
            isLoading = isLoading,
            dataSource = source
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicineSearchUiState())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
            loading.value = false
            if (repository.syncFirebaseOnce()) {
                dataSource.value = "Room + Firebase"
            }
        }
    }

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun clearQuery() {
        query.value = ""
    }
}
