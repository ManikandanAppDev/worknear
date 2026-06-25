package com.worknear.app

import android.app.Application
import com.worknear.app.di.AppContainer
import com.worknear.app.ui.address.AddressStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkNearApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Load persisted tokens into memory so the auth interceptor can attach them immediately.
        applicationScope.launch {
            container.tokenStore.warmCache()
            // Saved addresses live behind the auth token, so load them once it's warm.
            AddressStore.bind(container.accountRepository, applicationScope)
        }
    }
}
