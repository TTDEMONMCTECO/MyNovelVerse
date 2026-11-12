package com.example.novelverse.core.network

import com.example.novelverse.core.datastore.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class TokenManager(private val preferencesManager: PreferencesManager) {

    fun getAccessToken(): String? = runBlocking {
        preferencesManager.accessTokenFlow.firstOrNull()
    }

    fun getRefreshToken(): String? = runBlocking {
        preferencesManager.refreshTokenFlow.firstOrNull()
    }

    fun saveTokens(accessToken: String, refreshToken: String) = runBlocking {
        preferencesManager.saveAuthTokens(accessToken, refreshToken)
    }

    fun clearTokens() = runBlocking {
        preferencesManager.clearAuthTokens()
    }

    @Synchronized
    fun refreshToken(): String? {
        val currentRefresh = getRefreshToken()
        // If we have a refresh token (or simulate renewal for authorized sessions)
        val newAccess = "nv_access_" + System.currentTimeMillis()
        val newRefresh = currentRefresh ?: ("nv_refresh_" + System.currentTimeMillis())
        saveTokens(newAccess, newRefresh)
        return newAccess
    }
}
