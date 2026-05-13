package com.inventory.manager.utils

/**
 * Input validation utility for Add/Edit screens.
 */
object ValidationUtils {

    data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

    fun validateItemName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Item name cannot be empty.")
            name.length < 2 -> ValidationResult(false, "Item name must be at least 2 characters.")
            name.length > 100 -> ValidationResult(false, "Item name must be under 100 characters.")
            else -> ValidationResult(true)
        }
    }

    fun validateCategory(category: String): ValidationResult {
        return when {
            category.isBlank() -> ValidationResult(false, "Category cannot be empty.")
            else -> ValidationResult(true)
        }
    }

    fun validateQuantity(quantityStr: String): ValidationResult {
        return when {
            quantityStr.isBlank() -> ValidationResult(false, "Quantity cannot be empty.")
            quantityStr.toIntOrNull() == null -> ValidationResult(false, "Quantity must be a valid number.")
            quantityStr.toInt() < 0 -> ValidationResult(false, "Quantity cannot be negative.")
            else -> ValidationResult(true)
        }
    }

    fun validatePrice(priceStr: String): ValidationResult {
        return when {
            priceStr.isBlank() -> ValidationResult(false, "Price cannot be empty.")
            priceStr.toDoubleOrNull() == null -> ValidationResult(false, "Price must be a valid number.")
            priceStr.toDouble() < 0 -> ValidationResult(false, "Price cannot be negative.")
            else -> ValidationResult(true)
        }
    }

    fun validateSupplier(supplier: String): ValidationResult {
        return when {
            supplier.isBlank() -> ValidationResult(false, "Supplier cannot be empty.")
            else -> ValidationResult(true)
        }
    }
}
