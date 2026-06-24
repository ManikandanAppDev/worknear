package com.worknear.app

/** Prod environment — compiled only for the `prod` product flavor. */
object AppConfig {
    // Target prod API (update when prod Lightsail + DNS are ready):
    //   With domain:  https://api.worknear.in/
    //   Without domain: https://<PROD-IP-with-dashes>.sslip.io/
    const val BASE_URL = "https://api.worknear.in/"
    const val ENABLE_HTTP_LOGS = false
}
