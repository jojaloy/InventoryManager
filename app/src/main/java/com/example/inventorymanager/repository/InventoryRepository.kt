package com.inventory.manager.repository

import com.inventory.manager.api.RetrofitClient
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.InventoryItem
import com.inventory.manager.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository layer — handles all data operations.
 * Sits between ViewModel and the API service.
 * All functions run on the IO dispatcher.
 */
class InventoryRepository {

    private val api = RetrofitClient.apiService

    /**
     * Fetch all inventory items from the API.
     */
    suspend fun getAllItems(): ApiResult<List<InventoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllItems()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error("Failed to load items. Code: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(parseException(e))
        }
    }

    /**
     * Fetch a single item by its ID.
     */
    suspend fun getItemById(id: String): ApiResult<InventoryItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getItemById(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Item not found.")
            } else {
                ApiResult.Error("Failed to load item. Code: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(parseException(e))
        }
    }

    /**
     * Create a new inventory item.
     */
    suspend fun createItem(item: InventoryItem): ApiResult<InventoryItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createItem(item)
                if (response.isSuccessful) {
                    response.body()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Item created but response was empty.")
                } else {
                    ApiResult.Error("Failed to create item. Code: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                ApiResult.Error(parseException(e))
            }
        }

    /**
     * Update an existing inventory item.
     */
    suspend fun updateItem(id: String, item: InventoryItem): ApiResult<InventoryItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateItem(id, item)
                if (response.isSuccessful) {
                    response.body()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Update succeeded but response was empty.")
                } else {
                    ApiResult.Error("Failed to update item. Code: ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                ApiResult.Error(parseException(e))
            }
        }

    /**
     * Delete an inventory item by ID.
     */
    suspend fun deleteItem(id: String): ApiResult<InventoryItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteItem(id)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: InventoryItem())
            } else {
                ApiResult.Error("Failed to delete item. Code: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(parseException(e))
        }
    }

    // --- Auth Operations ---

    suspend fun login(username: String): ApiResult<User?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUsers(username)
            if (response.isSuccessful) {
                val users = response.body()
                if (!users.isNullOrEmpty()) {
                    ApiResult.Success(users.first())
                } else {
                    ApiResult.Error("User not found.")
                }
            } else {
                ApiResult.Error("Failed to login. Code: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(parseException(e))
        }
    }

    suspend fun register(user: User): ApiResult<User> = withContext(Dispatchers.IO) {
        try {
            // First check if the username already exists
            val existingUserResponse = api.getUsers(user.username)
            if (existingUserResponse.isSuccessful && !existingUserResponse.body().isNullOrEmpty()) {
                return@withContext ApiResult.Error("Username already exists. Please choose another.")
            }

            // Create new user
            val response = api.createUser(user)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Registration succeeded but response was empty.")
            } else {
                ApiResult.Error("Failed to register. Code: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(parseException(e))
        }
    }

    /**
     * Converts exceptions into readable messages.
     */
    private fun parseException(e: Exception): String {
        return when {
            e.message?.contains("Unable to resolve host") == true ->
                "No internet connection. Please check your network."
            e.message?.contains("timeout") == true ->
                "Request timed out. Please try again."
            e.message?.contains("Connection refused") == true ->
                "Server is unavailable. Please try later."
            else -> e.message ?: "An unexpected error occurred."
        }
    }
}