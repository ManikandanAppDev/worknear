package com.worknear.app.data.repository

import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.model.Transaction
import com.worknear.app.data.remote.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CustomerProfileSnapshot(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null
)

/**
 * Shared in-memory cache for customer tab data: profile, wallet, and bookings.
 * Updated after profile edits, bookings, and tab switches.
 */
class CustomerWorkspaceStore {

    private val refreshMutex = Mutex()

    private val _profile = MutableStateFlow(CustomerProfileSnapshot())
    val profile: StateFlow<CustomerProfileSnapshot> = _profile.asStateFlow()

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _upcomingBookings = MutableStateFlow<List<Booking>>(emptyList())
    val upcomingBookings: StateFlow<List<Booking>> = _upcomingBookings.asStateFlow()

    private val _completedBookings = MutableStateFlow<List<Booking>>(emptyList())
    val completedBookings: StateFlow<List<Booking>> = _completedBookings.asStateFlow()

    private val _cancelledBookings = MutableStateFlow<List<Booking>>(emptyList())
    val cancelledBookings: StateFlow<List<Booking>> = _cancelledBookings.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    suspend fun refresh(
        accountRepository: AccountRepository,
        tokenStore: TokenStore,
        walletRepository: WalletRepository,
        bookingRepository: BookingRepository
    ) {
        refreshMutex.withLock {
            _isRefreshing.value = true
            refreshProfile(accountRepository, tokenStore)
            refreshWallet(walletRepository)
            refreshBookings(bookingRepository)
            _isRefreshing.value = false
        }
    }

    suspend fun refreshProfile(accountRepository: AccountRepository, tokenStore: TokenStore) {
        val localAvatar = tokenStore.cachedAvatar()
        when (val me = accountRepository.getMe()) {
            is ApiResult.Success -> _profile.value = CustomerProfileSnapshot(
                name = me.data.fullName.orEmpty().ifBlank { "WorkNear User" },
                phone = me.data.phone.orEmpty(),
                email = me.data.email.orEmpty(),
                avatarUrl = localAvatar ?: me.data.avatarUrl
            )
            is ApiResult.Error -> Unit
        }
    }

    suspend fun refreshWallet(walletRepository: WalletRepository) {
        when (val balance = walletRepository.getBalance()) {
            is ApiResult.Success -> _walletBalance.value = balance.data
            is ApiResult.Error -> Unit
        }
        when (val txns = walletRepository.getTransactions()) {
            is ApiResult.Success -> _transactions.value = txns.data
            is ApiResult.Error -> Unit
        }
    }

    suspend fun refreshBookings(bookingRepository: BookingRepository) {
        when (val upcoming = bookingRepository.customerBookings("UPCOMING")) {
            is ApiResult.Success -> _upcomingBookings.value = upcoming.data
            is ApiResult.Error -> Unit
        }
        when (val completed = bookingRepository.customerBookings("COMPLETED")) {
            is ApiResult.Success -> _completedBookings.value = completed.data
            is ApiResult.Error -> Unit
        }
        when (val cancelled = bookingRepository.customerBookings("CANCELLED")) {
            is ApiResult.Success -> _cancelledBookings.value = cancelled.data
            is ApiResult.Error -> Unit
        }
    }

    suspend fun onBookingCreated(
        walletRepository: WalletRepository,
        bookingRepository: BookingRepository
    ) {
        refreshWallet(walletRepository)
        refreshBookings(bookingRepository)
    }

    suspend fun onBookingMutated(
        booking: Booking,
        walletRepository: WalletRepository
    ) {
        applyBookingUpdate(booking)
        refreshWallet(walletRepository)
    }

    fun updateProfile(
        name: String? = null,
        phone: String? = null,
        email: String? = null,
        avatarUrl: String? = null
    ) {
        _profile.update { current ->
            current.copy(
                name = name ?: current.name,
                phone = phone ?: current.phone,
                email = email ?: current.email,
                avatarUrl = avatarUrl ?: current.avatarUrl
            )
        }
    }

    fun applyBookingUpdate(booking: Booking) {
        when (booking.status) {
            BookingStatus.COMPLETED -> {
                _upcomingBookings.update { it.filter { job -> job.uuid != booking.uuid } }
                _completedBookings.update { listOf(booking) + it.filter { job -> job.uuid != booking.uuid } }
            }
            BookingStatus.CANCELLED, BookingStatus.REJECTED -> {
                _upcomingBookings.update { it.filter { job -> job.uuid != booking.uuid } }
                _cancelledBookings.update { listOf(booking) + it.filter { job -> job.uuid != booking.uuid } }
            }
            else -> {
                _upcomingBookings.update { jobs ->
                    if (jobs.any { it.uuid == booking.uuid }) {
                        jobs.map { if (it.uuid == booking.uuid) booking else it }
                    } else {
                        listOf(booking) + jobs
                    }
                }
            }
        }
    }

    fun clear() {
        _profile.value = CustomerProfileSnapshot()
        _walletBalance.value = 0.0
        _transactions.value = emptyList()
        _upcomingBookings.value = emptyList()
        _completedBookings.value = emptyList()
        _cancelledBookings.value = emptyList()
    }
}
