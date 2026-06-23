package com.worknear.app.data.repository

import com.worknear.app.data.model.Professional
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.ProfessionalDetailDto
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.toUiModel

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
}
