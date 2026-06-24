package com.worknear.app

/** Dev environment — compiled only for the `dev` product flavor. */
object AppConfig {
    // AWS Lightsail. For local emulator + Docker use: "http://10.0.2.2:8080/"
    const val BASE_URL = "https://65-1-135-244.sslip.io/"
    const val ENABLE_HTTP_LOGS = true
}
