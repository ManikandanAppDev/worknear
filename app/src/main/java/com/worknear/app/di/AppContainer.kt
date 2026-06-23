package com.worknear.app.di

import android.content.Context
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.AuthInterceptor
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.AuthRepository
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

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenStore))
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
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

    companion object {
        /**
         * Android emulator maps the host loopback to 10.0.2.2. For a physical device on the same
         * network, change this to your machine's LAN IP, e.g. "http://192.168.1.10:8080/".
         */
        const val BASE_URL = "http://10.0.2.2:8080/"
    }
}
