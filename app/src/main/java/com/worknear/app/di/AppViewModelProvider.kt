package com.worknear.app.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.worknear.app.WorkNearApplication
import com.worknear.app.ui.booking.BookServiceViewModel
import com.worknear.app.ui.booking.BookingConfirmedViewModel
import com.worknear.app.ui.bookings.BookingDetailViewModel
import com.worknear.app.ui.bookings.MyBookingsViewModel
import com.worknear.app.ui.home.HomeViewModel
import com.worknear.app.ui.login.LoginViewModel
import com.worknear.app.ui.main.MainViewModel
import com.worknear.app.ui.onboarding.CompleteProfileViewModel
import com.worknear.app.ui.professional.ProfessionalProfileViewModel
import com.worknear.app.ui.projobs.ProfessionalJobsViewModel
import com.worknear.app.ui.profile.ProfileViewModel
import com.worknear.app.ui.servicelist.ServiceListViewModel
import com.worknear.app.ui.wallet.WalletViewModel

/** Central factory that builds every ViewModel from the app's [AppContainer]. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { LoginViewModel(container().authRepository) }

        initializer { CompleteProfileViewModel(container().accountRepository) }

        initializer { MainViewModel(container().accountRepository) }

        initializer {
            HomeViewModel(
                container().catalogRepository,
                container().professionalRepository,
                container().accountRepository,
                container().bookingRepository
            )
        }

        initializer {
            ServiceListViewModel(
                container().professionalRepository,
                container().catalogRepository
            )
        }

        initializer { ProfessionalProfileViewModel(container().professionalRepository) }

        initializer {
            BookServiceViewModel(
                container().professionalRepository,
                container().accountRepository,
                container().bookingRepository
            )
        }

        initializer { BookingConfirmedViewModel(container().bookingRepository) }

        initializer { BookingDetailViewModel(container().bookingRepository) }

        initializer { MyBookingsViewModel(container().bookingRepository) }

        initializer { ProfessionalJobsViewModel(container().bookingRepository) }

        initializer { WalletViewModel(container().walletRepository) }

        initializer {
            ProfileViewModel(
                container().accountRepository,
                container().authRepository
            )
        }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as WorkNearApplication).container
