package com.inventory.manager.model

/**
 * A sealed class that wraps API results into three possible states:
 * - Success: the request succeeded and returned data
 * - Error: the request failed with a message
 * - Loading: the request is in progress
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
