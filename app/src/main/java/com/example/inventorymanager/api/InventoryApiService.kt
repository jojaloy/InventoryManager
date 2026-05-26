package com.inventory.manager.api

import com.inventory.manager.model.InventoryItem
import com.inventory.manager.model.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface.
 * Defines all HTTP endpoints for the inventory REST API.
 *
 * Base URL: https://mockapi.io (or your configured MockAPI endpoint)
 * This uses MockAPI.io — replace BASE_URL in RetrofitClient with your own endpoint.
 */
interface InventoryApiService {

    /**
     * GET all inventory items
     */
    @GET("items")
    suspend fun getAllItems(): Response<List<InventoryItem>>

    /**
     * GET a single inventory item by ID
     */
    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<InventoryItem>

    /**
     * POST — Create a new inventory item
     */
    @POST("items")
    suspend fun createItem(@Body item: InventoryItem): Response<InventoryItem>

    /**
     * PUT — Fully update an existing inventory item
     */
    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body item: InventoryItem
    ): Response<InventoryItem>

    /**
     * DELETE — Remove an inventory item by ID
     */
    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<InventoryItem>

    /**
     * GET users by username (used for login and checking if a user exists)
     */
    @GET("users")
    suspend fun getUsers(@Query("username") username: String): Response<List<User>>

    /**
     * POST — Create a new user (Registration)
     */
    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>
}