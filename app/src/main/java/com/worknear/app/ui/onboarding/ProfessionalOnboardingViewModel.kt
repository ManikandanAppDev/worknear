package com.worknear.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.ProServiceItemBody
import com.worknear.app.data.remote.dto.UpdateProProfileBody
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.CatalogRepository
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ProOnboardStep { PROFILE, DOCUMENTS }

/** A document the pro picked from their gallery, copied to a local file ready for upload. */
data class ProDocumentPick(
    val type: String,
    val file: File,
    val mimeType: String?,
    val displayName: String
)

data class CategoryOption(
    val id: String,
    val name: String,
    val basePrice: Double
)

data class ProfessionalOnboardingUiState(
    val step: ProOnboardStep = ProOnboardStep.PROFILE,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val fullName: String = "",
    val city: String = "",
    val area: String = "",
    val experience: String = "",
    val bio: String = "",
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val documents: Map<String, ProDocumentPick> = emptyMap()
)

/**
 * Drives the professional onboarding flow: (1) profile + services, (2) verification documents.
 * Every step is persisted to the backend via the real professional and account endpoints,
 * and finishes by submitting the profile for admin review.
 */
class ProfessionalOnboardingViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val accountRepository: AccountRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalOnboardingUiState())
    val uiState: StateFlow<ProfessionalOnboardingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // Prefill the name from the account, and any saved profile fields.
            val name = (accountRepository.getMe() as? ApiResult.Success)?.data?.fullName.orEmpty()
            val proProfile = (professionalRepository.getMyProfile() as? ApiResult.Success)?.data
            val categories = when (val r = catalogRepository.getCategoriesRaw()) {
                is ApiResult.Success -> r.data.mapNotNull { c ->
                    val id = c.id ?: return@mapNotNull null
                    CategoryOption(id = id, name = c.name.orEmpty(), basePrice = c.basePrice)
                }
                is ApiResult.Error -> emptyList()
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    fullName = name.ifBlank { it.fullName },
                    city = proProfile?.city ?: it.city,
                    area = proProfile?.area ?: it.area,
                    experience = proProfile?.experienceYears?.takeIf { y -> y > 0 }?.toString() ?: it.experience,
                    bio = proProfile?.bio ?: it.bio,
                    categories = categories,
                    selectedCategoryIds = proProfile?.services
                        ?.mapNotNull { s -> s.categoryId }?.toSet() ?: it.selectedCategoryIds
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(fullName = value, errorMessage = null) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value, errorMessage = null) }
    fun onAreaChange(value: String) = _uiState.update { it.copy(area = value, errorMessage = null) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, errorMessage = null) }
    fun onExperienceChange(value: String) =
        _uiState.update { it.copy(experience = value.filter { ch -> ch.isDigit() }.take(2), errorMessage = null) }

    fun toggleCategory(id: String) {
        _uiState.update {
            val next = it.selectedCategoryIds.toMutableSet()
            if (!next.add(id)) next.remove(id)
            it.copy(selectedCategoryIds = next, errorMessage = null)
        }
    }

    fun onDocumentPicked(pick: ProDocumentPick) {
        _uiState.update { it.copy(documents = it.documents + (pick.type to pick), errorMessage = null) }
    }

    fun backToProfile() = _uiState.update { it.copy(step = ProOnboardStep.PROFILE, errorMessage = null) }

    /** Persists the profile + chosen services, then advances to the documents step. */
    fun saveProfileAndContinue() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your full name") }
            return
        }
        if (state.selectedCategoryIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Select at least one service you offer") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val nameResult = accountRepository.updateProfile(UpdateProfileBody(fullName = state.fullName.trim()))
            if (nameResult is ApiResult.Error) return@launch fail(nameResult.message)

            val profileResult = professionalRepository.updateMyProfile(
                UpdateProProfileBody(
                    bio = state.bio.trim().ifBlank { null },
                    experienceYears = state.experience.toIntOrNull() ?: 0,
                    city = state.city.trim().ifBlank { null },
                    area = state.area.trim().ifBlank { null }
                )
            )
            if (profileResult is ApiResult.Error) return@launch fail(profileResult.message)

            val services = state.selectedCategoryIds.map { id ->
                val price = state.categories.firstOrNull { it.id == id }?.basePrice ?: 0.0
                ProServiceItemBody(categoryId = id, basePrice = price)
            }
            when (val servicesResult = professionalRepository.setServices(services)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, step = ProOnboardStep.DOCUMENTS)
                }
                is ApiResult.Error -> fail(servicesResult.message)
            }
        }
    }

    /** Uploads the picked documents, then submits the profile for admin verification. */
    fun submit(onCompleted: () -> Unit) {
        val state = _uiState.value
        if (state.documents.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Upload at least one verification document") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            for (doc in state.documents.values) {
                val result = professionalRepository.uploadDocument(doc.type, doc.file, doc.mimeType)
                if (result is ApiResult.Error) return@launch fail(result.message)
            }
            when (val submit = professionalRepository.submitForVerification()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onCompleted()
                }
                is ApiResult.Error -> fail(submit.message)
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { it.copy(isSubmitting = false, errorMessage = message) }
    }
}
