package com.worknear.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.ProServiceItemBody
import com.worknear.app.data.remote.dto.UpdateProProfileBody
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.CatalogRepository
import com.worknear.app.data.repository.ProfessionalRepository
import com.worknear.app.utils.ResolvedLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ProOnboardStep { PROFILE, DOCUMENTS }

enum class LocationFetchState {
    NOT_STARTED,
    FETCHING,
    SUCCESS,
    DENIED,
    FAILED
}

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

data class ExperienceOption(
    val label: String,
    val years: Int
)

val PRO_EXPERIENCE_OPTIONS = listOf(
    ExperienceOption("Less than 1 year", 0),
    ExperienceOption("1–2 Years", 2),
    ExperienceOption("3–5 Years", 4),
    ExperienceOption("5+ Years", 5)
)

data class ProfessionalOnboardingUiState(
    val step: ProOnboardStep = ProOnboardStep.PROFILE,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val fullName: String = "",
    val avatarUri: String? = null,
    val city: String = "",
    val area: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationState: LocationFetchState = LocationFetchState.NOT_STARTED,
    val locationPromptStarted: Boolean = false,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val selectedExperienceYears: Int? = null,
    val documents: Map<String, ProDocumentPick> = emptyMap()
) {
    val isProfileStepComplete: Boolean
        get() = fullName.isNotBlank() &&
            selectedCategoryIds.isNotEmpty() &&
            selectedExperienceYears != null &&
            city.isNotBlank() &&
            area.isNotBlank() &&
            locationState == LocationFetchState.SUCCESS
}

class ProfessionalOnboardingViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val accountRepository: AccountRepository,
    private val catalogRepository: CatalogRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalOnboardingUiState())
    val uiState: StateFlow<ProfessionalOnboardingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val localAvatar = tokenStore.cachedAvatar()
            val name = (accountRepository.getMe() as? ApiResult.Success)?.data?.fullName.orEmpty()
            val proProfile = (professionalRepository.getMyProfile() as? ApiResult.Success)?.data
            val categories = when (val r = catalogRepository.getCategoriesRaw()) {
                is ApiResult.Success -> r.data.mapNotNull { c ->
                    val id = c.id ?: return@mapNotNull null
                    CategoryOption(id = id, name = c.name.orEmpty(), basePrice = c.basePrice)
                }
                is ApiResult.Error -> emptyList()
            }
            val savedIds = proProfile?.services?.mapNotNull { it.categoryId }?.toSet().orEmpty()
            val savedYears = proProfile?.experienceYears?.takeIf { it > 0 }
            val savedCity = proProfile?.city.orEmpty()
            val savedArea = proProfile?.area.orEmpty()
            val hasSavedLocation = savedCity.isNotBlank() && savedArea.isNotBlank()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    fullName = name.ifBlank { it.fullName },
                    avatarUri = localAvatar,
                    categories = categories,
                    selectedCategoryIds = savedIds.ifEmpty { it.selectedCategoryIds },
                    selectedExperienceYears = savedYears ?: it.selectedExperienceYears,
                    city = savedCity,
                    area = savedArea,
                    locationState = if (hasSavedLocation) LocationFetchState.SUCCESS else it.locationState
                )
            }
        }
    }

    fun markLocationPromptStarted() {
        _uiState.update { it.copy(locationPromptStarted = true) }
    }

    fun beginLocationFetch() {
        _uiState.update {
            it.copy(locationState = LocationFetchState.FETCHING, errorMessage = null)
        }
    }

    fun onLocationResolved(location: ResolvedLocation) {
        _uiState.update {
            it.copy(
                city = location.city,
                area = location.area,
                latitude = location.latitude,
                longitude = location.longitude,
                locationState = LocationFetchState.SUCCESS,
                errorMessage = null
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                city = "",
                area = "",
                latitude = null,
                longitude = null,
                locationState = LocationFetchState.DENIED
            )
        }
    }

    fun onLocationFetchFailed() {
        _uiState.update {
            it.copy(
                city = "",
                area = "",
                latitude = null,
                longitude = null,
                locationState = LocationFetchState.FAILED
            )
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(fullName = value, errorMessage = null) }

    fun toggleCategory(id: String) {
        _uiState.update {
            val next = it.selectedCategoryIds.toMutableSet()
            if (!next.add(id)) next.remove(id)
            it.copy(selectedCategoryIds = next, errorMessage = null)
        }
    }

    fun onExperienceSelected(years: Int) =
        _uiState.update { it.copy(selectedExperienceYears = years, errorMessage = null) }

    fun onAvatarPicked(uri: String) {
        viewModelScope.launch {
            tokenStore.saveAvatar(uri)
            _uiState.update { it.copy(avatarUri = uri, errorMessage = null) }
        }
    }

    fun onDocumentPicked(pick: ProDocumentPick) {
        _uiState.update { it.copy(documents = it.documents + (pick.type to pick), errorMessage = null) }
    }

    fun backToProfile() = _uiState.update { it.copy(step = ProOnboardStep.PROFILE, errorMessage = null) }

    fun saveProfileAndContinue() {
        val state = _uiState.value
        if (!state.isProfileStepComplete) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val nameResult = accountRepository.updateProfile(
                UpdateProfileBody(
                    fullName = state.fullName.trim(),
                    avatarUrl = state.avatarUri
                )
            )
            if (nameResult is ApiResult.Error) return@launch fail(nameResult.message)

            tokenStore.updateProfileLocal(name = state.fullName.trim())

            val profileResult = professionalRepository.updateMyProfile(
                UpdateProProfileBody(
                    experienceYears = state.selectedExperienceYears,
                    city = state.city.trim(),
                    area = state.area.trim(),
                    baseLatitude = state.latitude,
                    baseLongitude = state.longitude
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

    fun submit(onCompleted: () -> Unit) {
        val state = _uiState.value
        if (state.documents["GOV_ID"] == null) {
            _uiState.update { it.copy(errorMessage = "Upload your Aadhar card to continue") }
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
                    tokenStore.clearPendingRoleSelection()
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
