package com.example.novelverse.core.network

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("User-Agent", "NovelVerse-App/1.0.0 (Android; Mobile; NovelReader)")
            .header("Accept", "application/json, text/html, application/xhtml+xml, */*")
            .header("X-App-Version", "1.0.0")
            .header("X-Platform", "Android")

        return chain.proceed(requestBuilder.build())
    }
}
