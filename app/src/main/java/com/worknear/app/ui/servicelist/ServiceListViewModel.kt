package com.worknear.app.ui.servicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Professional
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.CatalogRepository
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceListUiState(
    val title: String = "Services",
    val professionals: List<Professional> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ServiceListViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceListUiState())
    val uiState: StateFlow<ServiceListUiState> = _uiState.asStateFlow()

    private var loadedSlug: String? = null

    fun load(categorySlug: String) {
        if (loadedSlug == categorySlug) return
        loadedSlug = categorySlug
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val title = resolveTitle(categorySlug)
            _uiState.update { it.copy(title = title) }
            when (val result = professionalRepository.searchByCategory(categorySlug, title, sort = "RECOMMENDED")) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, professionals = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun resolveTitle(slug: String): String {
        return when (val cats = catalogRepository.getCategories()) {
            is ApiResult.Success -> cats.data.firstOrNull { it.id == slug }?.title
                ?: slug.replaceFirstChar { it.uppercase() }
            is ApiResult.Error -> slug.replaceFirstChar { it.uppercase() }
        }
    }
}
