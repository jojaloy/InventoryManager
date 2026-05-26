package com.inventory.manager.ui.add_edit

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.R
import com.inventory.manager.databinding.ActivityAddEditBinding
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.InventoryItem
import com.inventory.manager.utils.Constants
import com.inventory.manager.utils.DateUtils
import com.inventory.manager.utils.ValidationUtils
import com.inventory.manager.utils.gone
import com.inventory.manager.utils.hideKeyboard
import com.inventory.manager.utils.snackbar
import com.inventory.manager.utils.visible
import com.inventory.manager.viewmodel.InventoryViewModel

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    private var isEdit = false
    private var itemId: String = ""
    private var existingItem: InventoryItem? = null

    // Retrieve the user ID from SharedPreferences
    private val currentUserId: String
        get() = prefs.getString("PREF_USER_ID", prefs.getString(Constants.PREF_USERNAME, "")) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        isEdit = intent.getBooleanExtra(Constants.EXTRA_IS_EDIT, false)
        itemId = intent.getStringExtra(Constants.EXTRA_ITEM_ID) ?: ""

        setupToolbar()
        setupCategoryDropdown()
        setupSaveButton()
        observeViewModel()

        if (isEdit && itemId.isNotEmpty()) {
            viewModel.loadItemById(itemId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEdit) "Edit Item" else "Add New Item"
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            Constants.CATEGORIES
        )
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            hideKeyboard()
            if (validateInputs()) {
                saveItem()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val nameResult = ValidationUtils.validateItemName(binding.etItemName.text.toString())
        if (!nameResult.isValid) {
            binding.tilItemName.error = nameResult.errorMessage
            isValid = false
        } else binding.tilItemName.error = null

        val categoryResult = ValidationUtils.validateCategory(binding.actvCategory.text.toString())
        if (!categoryResult.isValid) {
            binding.tilCategory.error = categoryResult.errorMessage
            isValid = false
        } else binding.tilCategory.error = null

        val qtyResult = ValidationUtils.validateQuantity(binding.etQuantity.text.toString())
        if (!qtyResult.isValid) {
            binding.tilQuantity.error = qtyResult.errorMessage
            isValid = false
        } else binding.tilQuantity.error = null

        val priceResult = ValidationUtils.validatePrice(binding.etPrice.text.toString())
        if (!priceResult.isValid) {
            binding.tilPrice.error = priceResult.errorMessage
            isValid = false
        } else binding.tilPrice.error = null

        val supplierResult = ValidationUtils.validateSupplier(binding.etSupplier.text.toString())
        if (!supplierResult.isValid) {
            binding.tilSupplier.error = supplierResult.errorMessage
            isValid = false
        } else binding.tilSupplier.error = null

        return isValid
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().toInt()
        val price = binding.etPrice.text.toString().toDouble()
        val supplier = binding.etSupplier.text.toString().trim()
        val inStock = binding.switchInStock.isChecked
        val dateAdded = if (isEdit && existingItem != null) {
            existingItem!!.dateAdded
        } else {
            DateUtils.getCurrentDate()
        }

        // Make sure to preserve existing userId on edit, or assign current on creation
        val itemUserId = if (isEdit && existingItem != null) {
            existingItem!!.userId
        } else {
            currentUserId
        }

        val item = InventoryItem(
            id = if (isEdit) itemId else "",
            userId = itemUserId, // Pass the user ID to the item
            itemName = name,
            category = category,
            quantity = quantity,
            price = price,
            supplier = supplier,
            dateAdded = dateAdded,
            inStock = inStock
        )

        if (isEdit) {
            viewModel.updateItem(itemId, item, currentUserId)
        } else {
            viewModel.createItem(item, currentUserId)
        }
    }

    private fun observeViewModel() {
        // Observe item loading (for edit mode)
        viewModel.selectedItem.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                }
                is ApiResult.Success -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    existingItem = result.data
                    populateFields(result.data)
                }
                is ApiResult.Error -> {
                    binding.progressBar.gone()
                    binding.root.snackbar("Failed to load item: ${result.message}")
                    finish()
                }
            }
        }

        // Observe create/update results
        viewModel.operationResult.observe(this) { result ->
            result ?: return@observe
            when (result) {
                is ApiResult.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is ApiResult.Success -> {
                    binding.progressBar.gone()
                    val msg = if (isEdit) "Item updated successfully!" else "Item added successfully!"
                    binding.root.snackbar(msg)
                    viewModel.resetOperationResult()
                    finish()
                }
                is ApiResult.Error -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = if (isEdit) "Update Item" else "Save Item"
                    binding.root.snackbar("Error: ${result.message}")
                    viewModel.resetOperationResult()
                }
            }
        }
    }

    private fun populateFields(item: InventoryItem) {
        binding.etItemName.setText(item.itemName)
        binding.actvCategory.setText(item.category, false)
        binding.etQuantity.setText(item.quantity.toString())
        binding.etPrice.setText(item.price.toString())
        binding.etSupplier.setText(item.supplier)
        binding.switchInStock.isChecked = item.inStock
        binding.btnSave.text = "Update Item"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}