package com.inventory.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.InventoryItem
import com.inventory.manager.repository.InventoryRepository
import kotlinx.coroutines.launch

/**
 * InventoryViewModel — MVVM ViewModel layer.
 * Holds UI state and mediates between the UI and Repository.
 * Survives configuration changes (screen rotations).
 */
class InventoryViewModel : ViewModel() {

    private val repository = InventoryRepository()

    // --- LiveData for the full item list ---
    private val _items = MutableLiveData<ApiResult<List<InventoryItem>>>()
    val items: LiveData<ApiResult<List<InventoryItem>>> = _items

    // --- LiveData for a single item ---
    private val _selectedItem = MutableLiveData<ApiResult<InventoryItem>>()
    val selectedItem: LiveData<ApiResult<InventoryItem>> = _selectedItem

    // --- LiveData for create/update/delete operations ---
    private val _operationResult = MutableLiveData<ApiResult<InventoryItem>>()
    val operationResult: LiveData<ApiResult<InventoryItem>> = _operationResult

    // --- Local filtered list for search ---
    private var allItems: List<InventoryItem> = emptyList()
    private val _filteredItems = MutableLiveData<List<InventoryItem>>()
    val filteredItems: LiveData<List<InventoryItem>> = _filteredItems

    // Current search query
    var currentSearchQuery: String = ""

    /**
     * Load all items from the API and filter them by the logged-in userId.
     */
    fun loadAllItems(userId: String) {
        _items.value = ApiResult.Loading
        viewModelScope.launch {
            val result = repository.getAllItems()

            if (result is ApiResult.Success) {
                // Filter the list so we only keep items belonging to this user
                val userItems = result.data.filter { it.userId == userId }
                allItems = userItems
                _items.value = ApiResult.Success(userItems)
                applyFilter(currentSearchQuery)
            } else {
                _items.value = result // Propagate the error
            }
        }
    }

    /**
     * Load a single item by ID.
     */
    fun loadItemById(id: String) {
        _selectedItem.value = ApiResult.Loading
        viewModelScope.launch {
            _selectedItem.value = repository.getItemById(id)
        }
    }

    /**
     * Create a new inventory item.
     */
    fun createItem(item: InventoryItem, currentUserId: String) {
        _operationResult.value = ApiResult.Loading
        viewModelScope.launch {
            val result = repository.createItem(item)
            _operationResult.value = result
            if (result is ApiResult.Success) {
                loadAllItems(currentUserId) // Refresh the list for this user
            }
        }
    }

    /**
     * Update an existing inventory item.
     */
    fun updateItem(id: String, item: InventoryItem, currentUserId: String) {
        _operationResult.value = ApiResult.Loading
        viewModelScope.launch {
            val result = repository.updateItem(id, item)
            _operationResult.value = result
            if (result is ApiResult.Success) {
                loadAllItems(currentUserId) // Refresh the list for this user
            }
        }
    }

    /**
     * Delete an inventory item.
     */
    fun deleteItem(id: String, currentUserId: String) {
        _operationResult.value = ApiResult.Loading
        viewModelScope.launch {
            val result = repository.deleteItem(id)
            _operationResult.value = result
            if (result is ApiResult.Success) {
                loadAllItems(currentUserId) // Refresh the list for this user
            }
        }
    }

    /**
     * Filter items by search query (searches itemName, category, supplier).
     */
    fun applyFilter(query: String) {
        currentSearchQuery = query
        _filteredItems.value = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                item.itemName.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.supplier.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Sort items by a given field.
     */
    fun sortItems(sortBy: SortOption) {
        val sorted = when (sortBy) {
            SortOption.NAME_ASC  -> allItems.sortedBy { it.itemName }
            SortOption.NAME_DESC -> allItems.sortedByDescending { it.itemName }
            SortOption.PRICE_ASC  -> allItems.sortedBy { it.price }
            SortOption.PRICE_DESC -> allItems.sortedByDescending { it.price }
            SortOption.QTY_ASC  -> allItems.sortedBy { it.quantity }
            SortOption.QTY_DESC -> allItems.sortedByDescending { it.quantity }
        }
        allItems = sorted
        applyFilter(currentSearchQuery)
    }

    /**
     * Reset the operation result (avoids re-triggering on recompose).
     */
    fun resetOperationResult() {
        _operationResult.value = null
    }

    enum class SortOption {
        NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, QTY_ASC, QTY_DESC
    }
}