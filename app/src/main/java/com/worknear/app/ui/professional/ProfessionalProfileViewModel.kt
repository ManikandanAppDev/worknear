package com.worknear.app.ui.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Professional
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfessionalProfileUiState(
    val professional: Professional? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfessionalProfileViewModel(
    private val professionalRepository: ProfessionalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalProfileUiState())
    val uiState: StateFlow<ProfessionalProfileUiState> = _uiState.asStateFlow()

    private var loadedId: String? = null

    fun load(userId: String) {
        if (loadedId == userId) return
        loadedId = userId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = professionalRepository.getDetail(userId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, professional = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
