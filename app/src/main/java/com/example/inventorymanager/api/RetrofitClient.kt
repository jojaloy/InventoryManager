package com.inventory.manager.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton.
 *
 * HOW TO CONNECT YOUR REST API:
 * 1. Go to https://mockapi.io and create a free account.
 * 2. Create a new project and add a resource called "items".
 * 3. Add fields: itemName, category, quantity, price, supplier, dateAdded, inStock.
 * 4. Copy your project's base URL (e.g., https://64abc123.mockapi.io/api/v1/).
 * 5. Replace BASE_URL below with your MockAPI URL.
 *
 * Alternative: Use https://reqres.in or your own JSON Server.
 */
object RetrofitClient {

    // ⚠️ REPLACE THIS WITH YOUR MOCKAPI BASE URL
    private const val BASE_URL = "https://6a04386e2afe8349b4b6229a.mockapi.io/api/v1/"

    /**
     * OkHttpClient with logging + timeouts
     */
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Retrofit instance (lazy singleton)
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Exposes the API service
     */
    val apiService: InventoryApiService by lazy {
        retrofit.create(InventoryApiService::class.java)
    }
}
