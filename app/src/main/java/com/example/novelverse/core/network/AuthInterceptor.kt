package com.example.novelverse.core.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val authenticatedRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        var response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized: token expired, attempt refresh and retry
        if (response.code == 401) {
            response.close() // Close previous response body before retrying

            synchronized(this) {
                val newToken = tokenManager.refreshToken()
                if (!newToken.isNullOrBlank()) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    response = chain.proceed(retryRequest)
                }
            }
        }

        return response
    }
}
