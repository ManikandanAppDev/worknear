package com.worknear.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.BannerDto
import com.worknear.app.data.repository.BannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val banners: List<BannerDto> = emptyList()
)

/** Loads the admin-managed onboarding carousel slides from the backend. */
class OnboardingViewModel(
    private val bannerRepository: BannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = bannerRepository.getOnboardingBanners(audience = "ALL")) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, banners = result.data.filter { b -> !b.imageUrl.isNullOrBlank() })
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
