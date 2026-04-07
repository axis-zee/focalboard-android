package com.focalboard.android.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiService {
    
    // Simple in-memory cookie jar for CSRF token handling
    private val cookieJar: CookieJar = object : CookieJar {
        private val cookies = mutableMapOf<String, MutableMap<String, Cookie>>()
        
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val domain = url.host
            this.cookies.getOrPut(domain) { mutableMapOf() }
                .putAll(cookies.associateBy { it.name })
        }
        
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val domain = url.host
            return cookies[domain]?.values?.toList() ?: emptyList()
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun getFocalboardApi(baseUrl: String): FocalboardApi {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return createRetrofit(normalizedUrl).create(FocalboardApi::class.java)
    }
}
