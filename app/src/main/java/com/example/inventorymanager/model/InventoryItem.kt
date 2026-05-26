package com.inventory.manager.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing a single inventory item.
 * Maps directly to the MockAPI JSON structure.
 */
data class InventoryItem(
    @SerializedName("id")
    val id: String = "",

    // Added userId to tie items to specific users
    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("itemName")
    val itemName: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("quantity")
    val quantity: Int = 0,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("supplier")
    val supplier: String = "",

    @SerializedName("dateAdded")
    val dateAdded: String = "",

    @SerializedName("inStock")
    val inStock: Boolean = true
) {
    /**
     * Returns a formatted price string (e.g., "$12.99")
     */
    fun formattedPrice(): String = "$${"%.2f".format(price)}"

    /**
     * Returns stock status label
     */
    fun stockStatus(): String = if (inStock) "In Stock" else "Out of Stock"
}