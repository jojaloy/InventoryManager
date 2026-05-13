package com.inventory.manager.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Date utility helpers.
 */
object DateUtils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun getCurrentDate(): String = sdf.format(Date())

    fun formatForDisplay(dateStr: String): String {
        return try {
            val date = sdf.parse(dateStr)
            if (date != null) displaySdf.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}

/**
 * App-wide constants.
 */
object Constants {
    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_IS_EDIT = "extra_is_edit"
    const val PREF_NAME = "inventory_prefs"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_USERNAME = "username"

    val CATEGORIES = listOf(
        "Electronics",
        "Clothing",
        "Food & Beverage",
        "Furniture",
        "Tools & Hardware",
        "Office Supplies",
        "Health & Beauty",
        "Sports & Outdoors",
        "Books & Media",
        "Automotive",
        "Other"
    )
}
