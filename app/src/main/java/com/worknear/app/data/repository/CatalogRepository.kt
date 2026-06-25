package com.worknear.app.data.repository

import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.CategoryDto
import com.worknear.app.data.remote.dto.OfferDto
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.toUiModel

class CatalogRepository(private val api: WorkNearApi) {

    suspend fun getCategories(): ApiResult<List<ServiceCategory>> {
        return when (val r = safeCall { api.categories() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.map { it.toUiModel() })
            is ApiResult.Error -> r
        }
    }

    /** Raw categories (with id + base price), used by professional onboarding to pick services. */
    suspend fun getCategoriesRaw(): ApiResult<List<CategoryDto>> = safeCall { api.categories() }

    suspend fun getOffers(): ApiResult<List<OfferDto>> = safeCall { api.offers() }
}
