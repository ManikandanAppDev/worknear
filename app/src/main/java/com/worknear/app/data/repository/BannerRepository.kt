package com.worknear.app.data.repository

import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.BannerDto
import com.worknear.app.data.remote.safeCall

/** Onboarding carousel slides, fully admin-managed via the backend. Served pre-login (public). */
class BannerRepository(private val api: WorkNearApi) {

    suspend fun getOnboardingBanners(audience: String = "ALL"): ApiResult<List<BannerDto>> =
        safeCall { api.banners(audience) }
}
