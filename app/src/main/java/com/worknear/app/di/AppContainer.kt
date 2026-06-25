package com.worknear.app.di

import android.content.Context
import com.worknear.app.AppConfig
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.AuthInterceptor
import com.worknear.app.data.remote.AuthRefreshApi
import com.worknear.app.data.remote.TokenAuthenticator
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.AuthRepository
import com.worknear.app.data.repository.BannerRepository
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.CatalogRepository
import com.worknear.app.data.repository.ProfessionalRepository
import com.worknear.app.data.repository.WalletRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Lightweight manual DI container. Holds the singletons (network, storage, repositories)
 * for the app's lifetime. Exposed via [com.worknear.app.WorkNearApplication].
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val tokenStore: TokenStore = TokenStore(appContext)

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // Bare client used only to refresh tokens. No auth interceptor/authenticator -> no recursion.
    private val refreshHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        if (AppConfig.ENABLE_HTTP_LOGS) addInterceptor(loggingInterceptor())
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
    }.build()

    private val authRefreshApi: AuthRefreshApi = Retrofit.Builder()
        .baseUrl(AppConfig.BASE_URL)
        .client(refreshHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthRefreshApi::class.java)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(AuthInterceptor(tokenStore))
        authenticator(TokenAuthenticator(tokenStore, authRefreshApi))
        if (AppConfig.ENABLE_HTTP_LOGS) addInterceptor(loggingInterceptor())
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
    }.build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: WorkNearApi = retrofit.create(WorkNearApi::class.java)

    val authRepository = AuthRepository(api, tokenStore)
    val catalogRepository = CatalogRepository(api)
    val professionalRepository = ProfessionalRepository(api)
    val bookingRepository = BookingRepository(api)
    val walletRepository = WalletRepository(api)
    val accountRepository = AccountRepository(api)
    val bannerRepository = BannerRepository(api)
}
