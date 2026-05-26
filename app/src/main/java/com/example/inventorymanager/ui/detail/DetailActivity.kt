package com.inventory.manager.ui.detail

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.R
import com.inventory.manager.databinding.ActivityDetailBinding
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.InventoryItem
import com.inventory.manager.ui.add_edit.AddEditActivity
import com.inventory.manager.utils.Constants
import com.inventory.manager.utils.DateUtils
import com.inventory.manager.utils.gone
import com.inventory.manager.utils.snackbar
import com.inventory.manager.utils.visible
import com.inventory.manager.viewmodel.InventoryViewModel

/**
 * DetailActivity — shows full details for a single inventory item.
 * Provides Edit and Delete actions.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var prefs: SharedPreferences
    private var itemId: String = ""
    private var currentItem: InventoryItem? = null

    // Retrieve the user ID from SharedPreferences
    private val currentUserId: String
        get() = prefs.getString("PREF_USER_ID", prefs.getString(Constants.PREF_USERNAME, "")) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        itemId = intent.getStringExtra(Constants.EXTRA_ITEM_ID) ?: ""

        setupToolbar()
        setupButtons()
        observeViewModel()

        viewModel.loadItemById(itemId)
    }

    override fun onResume() {
        super.onResume()
        // Reload item in case we are returning from AddEditActivity after an update
        if (itemId.isNotEmpty()) {
            viewModel.loadItemById(itemId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Item Details"
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AddEditActivity::class.java)
            intent.putExtra(Constants.EXTRA_ITEM_ID, itemId)
            intent.putExtra(Constants.EXTRA_IS_EDIT, true)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            currentItem?.let { item ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete \"${item.itemName}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        // Pass the current user ID to the delete function
                        viewModel.deleteItem(item.id, currentUserId)
                    }
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.ic_delete)
                    .show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.selectedItem.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.progressBar.visible()
                    binding.contentLayout.gone()
                }
                is ApiResult.Success -> {
                    binding.progressBar.gone()
                    binding.contentLayout.visible()
                    currentItem = result.data
                    displayItem(result.data)
                }
                is ApiResult.Error -> {
                    binding.progressBar.gone()
                    binding.root.snackbar("Error: ${result.message}")
                }
            }
        }

        viewModel.operationResult.observe(this) { result ->
            result ?: return@observe
            when (result) {
                is ApiResult.Loading -> {
                    binding.btnDelete.isEnabled = false
                }
                is ApiResult.Success -> {
                    binding.root.snackbar("Item deleted.")
                    viewModel.resetOperationResult()
                    finish()
                }
                is ApiResult.Error -> {
                    binding.btnDelete.isEnabled = true
                    binding.root.snackbar("Delete failed: ${result.message}")
                    viewModel.resetOperationResult()
                }
            }
        }
    }

    private fun displayItem(item: InventoryItem) {
        binding.apply {
            tvDetailName.text = item.itemName
            tvDetailCategory.text = item.category
            tvDetailQuantity.text = item.quantity.toString()
            tvDetailPrice.text = item.formattedPrice()
            tvDetailSupplier.text = item.supplier
            tvDetailDate.text = DateUtils.formatForDisplay(item.dateAdded)
            tvDetailId.text = "ID: ${item.id}"

            // Category initial
            tvCategoryInitialDetail.text = item.category.take(1).uppercase()

            // Stock badge
            if (item.inStock) {
                tvDetailStockStatus.text = "In Stock"
                tvDetailStockStatus.setBackgroundResource(R.drawable.bg_badge_in_stock)
            } else {
                tvDetailStockStatus.text = "Out of Stock"
                tvDetailStockStatus.setBackgroundResource(R.drawable.bg_badge_out_of_stock)
            }

            // Low stock warning
            if (item.quantity in 1..5) {
                tvDetailLowStock.visible()
                tvDetailLowStock.text = "⚠ Low stock — only ${item.quantity} remaining"
            } else if (item.quantity == 0) {
                tvDetailLowStock.visible()
                tvDetailLowStock.text = "⚠ Out of stock"
            } else {
                tvDetailLowStock.gone()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}