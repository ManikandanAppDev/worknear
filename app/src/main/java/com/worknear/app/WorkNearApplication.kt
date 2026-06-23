package com.worknear.app

import android.app.Application
import com.worknear.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkNearApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Load persisted tokens into memory so the auth interceptor can attach them immediately.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.tokenStore.warmCache()
        }
    }
}
