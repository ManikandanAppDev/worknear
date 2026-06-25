package com.worknear.app.data.repository

import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.AddressDto
import com.worknear.app.data.remote.dto.AddressRequestBody
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.remote.dto.UserDto
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.safeUnit

class AccountRepository(private val api: WorkNearApi) {

    suspend fun getMe(): ApiResult<UserDto> = safeCall { api.me() }

    suspend fun updateProfile(body: UpdateProfileBody): ApiResult<UserDto> =
        safeCall { api.updateProfile(body) }

    suspend fun getAddresses(): ApiResult<List<AddressDto>> = safeCall { api.addresses() }

    suspend fun createAddress(body: AddressRequestBody): ApiResult<AddressDto> =
        safeCall { api.createAddress(body) }

    suspend fun updateAddress(addressId: String, body: AddressRequestBody): ApiResult<AddressDto> =
        safeCall { api.updateAddress(addressId, body) }

    suspend fun deleteAddress(addressId: String): ApiResult<Unit> =
        safeUnit { api.deleteAddress(addressId) }

    /**
     * Whether the signed-in customer still needs the one-time onboarding step
     * (no name yet, or no saved address). On a network error we return false so
     * an existing user is never locked out of the app while offline.
     */
    suspend fun needsOnboarding(): Boolean {
        val me = getMe()
        if (me is ApiResult.Error) return false
        val nameMissing = (me as ApiResult.Success).data.fullName.isNullOrBlank()
        val noAddress = when (val addresses = getAddresses()) {
            is ApiResult.Success -> addresses.data.isEmpty()
            is ApiResult.Error -> false
        }
        return nameMissing || noAddress
    }
}
