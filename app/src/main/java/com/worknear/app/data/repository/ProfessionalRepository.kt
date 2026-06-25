package com.worknear.app.data.repository

import com.worknear.app.data.model.Professional
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.ProDocumentDto
import com.worknear.app.data.remote.dto.ProProfileDto
import com.worknear.app.data.remote.dto.ProServiceItemBody
import com.worknear.app.data.remote.dto.ProfessionalDetailDto
import com.worknear.app.data.remote.dto.SetProServicesBody
import com.worknear.app.data.remote.dto.UpdateProProfileBody
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.toUiModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfessionalRepository(private val api: WorkNearApi) {

    /** Search professionals within a category. [professionLabel] is used as the displayed profession. */
    suspend fun searchByCategory(
        categorySlug: String,
        professionLabel: String,
        sort: String = "RECOMMENDED"
    ): ApiResult<List<Professional>> {
        return when (val r = safeCall { api.searchProfessionals(categorySlug = categorySlug, sort = sort) }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.map { it.toUiModel(professionLabel = professionLabel, categorySlug = categorySlug) }
            )
            is ApiResult.Error -> r
        }
    }

    suspend fun getDetail(userId: String): ApiResult<Professional> {
        return when (val r = safeCall { api.professionalDetail(userId) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toUiModel())
            is ApiResult.Error -> r
        }
    }

    /** Raw detail, used by the booking flow to resolve the category UUID and base price. */
    suspend fun getDetailRaw(userId: String): ApiResult<ProfessionalDetailDto> =
        safeCall { api.professionalDetail(userId) }

    // ----- Professional self-service onboarding -----

    suspend fun getMyProfile(): ApiResult<ProProfileDto> = safeCall { api.proProfile() }

    suspend fun updateMyProfile(body: UpdateProProfileBody): ApiResult<ProProfileDto> =
        safeCall { api.updateProProfile(body) }

    suspend fun setServices(services: List<ProServiceItemBody>): ApiResult<ProProfileDto> =
        safeCall { api.setProServices(SetProServicesBody(services)) }

    /** Uploads a verification document. [type] must be GOV_ID, ADDRESS_PROOF or WORK_CERTIFICATE. */
    suspend fun uploadDocument(type: String, file: File, mimeType: String?): ApiResult<ProDocumentDto> {
        val body = file.asRequestBody(mimeType?.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        return safeCall { api.uploadProDocument(type, part) }
    }

    suspend fun submitForVerification(): ApiResult<ProProfileDto> =
        safeCall { api.submitProVerification() }
}
