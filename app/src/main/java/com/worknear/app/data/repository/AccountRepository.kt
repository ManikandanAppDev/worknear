package com.worknear.app.data.repository

import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.AddressDto
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.remote.dto.UserDto
import com.worknear.app.data.remote.safeCall

class AccountRepository(private val api: WorkNearApi) {

    suspend fun getMe(): ApiResult<UserDto> = safeCall { api.me() }

    suspend fun updateProfile(body: UpdateProfileBody): ApiResult<UserDto> =
        safeCall { api.updateProfile(body) }

    suspend fun getAddresses(): ApiResult<List<AddressDto>> = safeCall { api.addresses() }
}
