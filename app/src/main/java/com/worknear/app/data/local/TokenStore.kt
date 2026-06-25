package com.worknear.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore by preferencesDataStore(name = "worknear_session")

/**
 * Persists the auth session (tokens + lightweight user info) in DataStore, while keeping the
 * access token in memory so the OkHttp interceptor can attach it synchronously on each request.
 */
class TokenStore(private val context: Context) {

    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val userNameKey = stringPreferencesKey("user_name")
    private val userPhoneKey = stringPreferencesKey("user_phone")
    private val userRoleKey = stringPreferencesKey("user_role")
    private val userAvatarKey = stringPreferencesKey("user_avatar")

    private val cachedAccess = AtomicReference<String?>(null)
    private val cachedRefresh = AtomicReference<String?>(null)

    /** Synchronous access for the auth interceptor (kept in sync with DataStore). */
    val accessTokenSnapshot: String? get() = cachedAccess.get()
    val refreshTokenSnapshot: String? get() = cachedRefresh.get()

    /**
     * Returns the access token for the OkHttp interceptor, falling back to a blocking DataStore
     * read if the in-memory cache hasn't been warmed yet (avoids a cold-start race on first call).
     */
    fun accessTokenBlocking(): String? {
        cachedAccess.get()?.let { return it }
        return runBlocking {
            val token = context.dataStore.data.first()[accessKey]
            if (!token.isNullOrBlank()) cachedAccess.set(token)
            token
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { !it[accessKey].isNullOrBlank() }

    /** Loads persisted tokens into memory; call once on app start. */
    suspend fun warmCache() {
        val prefs = context.dataStore.data.first()
        cachedAccess.set(prefs[accessKey])
        cachedRefresh.set(prefs[refreshKey])
    }

    suspend fun hasSession(): Boolean {
        val prefs = context.dataStore.data.first()
        return !prefs[accessKey].isNullOrBlank()
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String?,
        userName: String?,
        userPhone: String?,
        userRole: String?
    ) {
        cachedAccess.set(accessToken)
        cachedRefresh.set(refreshToken)
        context.dataStore.edit { prefs ->
            prefs[accessKey] = accessToken
            prefs[refreshKey] = refreshToken
            userId?.let { prefs[userIdKey] = it }
            userName?.let { prefs[userNameKey] = it }
            userPhone?.let { prefs[userPhoneKey] = it }
            userRole?.let { prefs[userRoleKey] = it }
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        cachedAccess.set(accessToken)
        cachedRefresh.set(refreshToken)
        context.dataStore.edit { prefs ->
            prefs[accessKey] = accessToken
            prefs[refreshKey] = refreshToken
        }
    }

    suspend fun cachedUserName(): String? = context.dataStore.data.first()[userNameKey]

    /** Locally stored avatar (a file:// URI). Backend has no avatar upload, so this is device-local. */
    suspend fun cachedAvatar(): String? = context.dataStore.data.first()[userAvatarKey]

    suspend fun saveAvatar(uri: String) {
        context.dataStore.edit { it[userAvatarKey] = uri }
    }

    /** Updates locally cached profile fields (used after editing name / changing phone). */
    suspend fun updateProfileLocal(name: String? = null, phone: String? = null) {
        context.dataStore.edit { prefs ->
            name?.let { prefs[userNameKey] = it }
            phone?.let { prefs[userPhoneKey] = it }
        }
    }

    suspend fun clear() {
        cachedAccess.set(null)
        cachedRefresh.set(null)
        context.dataStore.edit { it.clear() }
    }
}
