package com.worknear.app.data.repository

import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.AddressDto
import com.worknear.app.data.remote.dto.AddressRequestBody
import com.worknear.app.data.remote.dto.AuthData
import com.worknear.app.data.remote.dto.ChangePhoneRequestBody
import com.worknear.app.data.remote.dto.ConfirmPhoneChangeBody
import com.worknear.app.data.remote.dto.OtpRequestData
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.remote.dto.UserDto
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.safeUnit

class AccountRepository(
    private val api: WorkNearApi,
    private val professionalRepository: ProfessionalRepository
) {

    suspend fun getMe(): ApiResult<UserDto> = safeCall { api.me() }

    suspend fun updateProfile(body: UpdateProfileBody): ApiResult<UserDto> =
        safeCall { api.updateProfile(body) }

    /** Sends an OTP to [phone] to begin a phone-number change. */
    suspend fun requestPhoneChangeOtp(phone: String): ApiResult<OtpRequestData> =
        safeCall { api.requestPhoneChangeOtp(ChangePhoneRequestBody(phone)) }

    /** Confirms the phone change; on success returns fresh tokens for the updated account. */
    suspend fun confirmPhoneChange(phone: String, code: String): ApiResult<AuthData> =
        safeCall { api.confirmPhoneChange(ConfirmPhoneChangeBody(phone, code)) }

    suspend fun getAddresses(): ApiResult<List<AddressDto>> = safeCall { api.addresses() }

    suspend fun createAddress(body: AddressRequestBody): ApiResult<AddressDto> =
        safeCall { api.createAddress(body) }

    suspend fun updateAddress(addressId: String, body: AddressRequestBody): ApiResult<AddressDto> =
        safeCall { api.updateAddress(addressId, body) }

    suspend fun deleteAddress(addressId: String): ApiResult<Unit> =
        safeUnit { api.deleteAddress(addressId) }

    /**
     * Whether the signed-in user still needs the post-OTP "Who are you?" role step.
     * Customers: until [UserDto.roleConfirmed]. Professionals: until verification is PENDING or APPROVED.
     */
    suspend fun needsRoleSelection(): Boolean {
        return when (val me = getMe()) {
            is ApiResult.Success -> {
                if (me.data.role.equals("PROFESSIONAL", ignoreCase = true)) {
                    professionalRepository.shouldShowRoleSelection()
                } else {
                    me.data.roleConfirmed != true
                }
            }
            is ApiResult.Error -> false
        }
    }

    /**
     * Whether the signed-in customer still needs the one-time onboarding step
     * (no name yet, or no saved address). On a network error we return false so
     * an existing user is never locked out of the app while offline.
     */
    suspend fun needsOnboarding(): Boolean {
        val me = getMe()
        if (me is ApiResult.Error) return false
        val user = (me as ApiResult.Success).data
        if (user.role.equals("PROFESSIONAL", ignoreCase = true)) return false
        val nameMissing = user.fullName.isNullOrBlank()
        val noAddress = when (val addresses = getAddresses()) {
            is ApiResult.Success -> addresses.data.isEmpty()
            is ApiResult.Error -> false
        }
        return nameMissing || noAddress
    }
}
