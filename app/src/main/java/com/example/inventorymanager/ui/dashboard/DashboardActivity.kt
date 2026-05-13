package com.inventory.manager.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventory.manager.R
import com.inventory.manager.adapter.InventoryAdapter
import com.inventory.manager.databinding.ActivityDashboardBinding
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.InventoryItem
import com.inventory.manager.ui.add_edit.AddEditActivity
import com.inventory.manager.ui.detail.DetailActivity
import com.inventory.manager.ui.login.LoginActivity
import com.inventory.manager.utils.Constants
import com.inventory.manager.utils.NetworkUtils
import com.inventory.manager.utils.gone
import com.inventory.manager.utils.snackbar
import com.inventory.manager.utils.visible
import com.inventory.manager.viewmodel.InventoryViewModel

/**
 * DashboardActivity — the main screen.
 * Shows the inventory list, search, FAB to add items, sort menu.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupSearch()
        setupSwipeRefresh()
        observeViewModel()

        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from Add/Edit/Detail
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val username = prefs.getString(Constants.PREF_USERNAME, "Admin") ?: "Admin"
        binding.tvWelcome.text = "Hello, $username 👋"
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(
            onItemClick = { item -> openDetail(item) },
            onEditClick = { item -> openEdit(item) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = this@DashboardActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabAddItem.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.applyFilter(s?.toString() ?: "")
            }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun observeViewModel() {
        // Observe full item loading state
        viewModel.items.observe(this) { result ->
            binding.swipeRefresh.isRefreshing = false
            when (result) {
                is ApiResult.Loading -> {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visible()
                    binding.recyclerView.gone()
                    binding.layoutEmpty.gone()
                    binding.layoutError.gone()
                }
                is ApiResult.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.gone()
                    updateStatsBar(result.data)
                }
                is ApiResult.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.gone()
                    binding.recyclerView.gone()
                    binding.layoutEmpty.gone()
                    binding.layoutError.visible()
                    binding.tvErrorMessage.text = result.message
                }
            }
        }

        // Observe filtered list (shown in RecyclerView)
        viewModel.filteredItems.observe(this) { items ->
            adapter.submitList(items)
            if (items.isEmpty()) {
                binding.recyclerView.gone()
                binding.layoutEmpty.visible()
            } else {
                binding.recyclerView.visible()
                binding.layoutEmpty.gone()
            }
        }

        // Observe CRUD operation results
        viewModel.operationResult.observe(this) { result ->
            result ?: return@observe
            when (result) {
                is ApiResult.Loading -> { /* handled per-operation */ }
                is ApiResult.Success -> {
                    binding.root.snackbar("Operation successful ✓")
                }
                is ApiResult.Error -> {
                    binding.root.snackbar("Error: ${result.message}", actionText = "Retry") {
                        // Retry will re-load
                        loadData()
                    }
                }
            }
            viewModel.resetOperationResult()
        }
    }

    private fun updateStatsBar(items: List<InventoryItem>) {
        val total = items.size
        val inStock = items.count { it.inStock }
        val lowStock = items.count { it.quantity in 1..5 }
        binding.tvStatTotal.text = total.toString()
        binding.tvStatInStock.text = inStock.toString()
        binding.tvStatLowStock.text = lowStock.toString()
    }

    private fun loadData() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            binding.swipeRefresh.isRefreshing = false
            binding.root.snackbar("No internet connection", actionText = "Retry") { loadData() }
            return
        }
        viewModel.loadAllItems()
    }

    private fun openDetail(item: InventoryItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.EXTRA_ITEM_ID, item.id)
        startActivity(intent)
    }

    private fun openEdit(item: InventoryItem) {
        val intent = Intent(this, AddEditActivity::class.java)
        intent.putExtra(Constants.EXTRA_ITEM_ID, item.id)
        intent.putExtra(Constants.EXTRA_IS_EDIT, true)
        startActivity(intent)
    }

    private fun confirmDelete(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete \"${item.itemName}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(item.id)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_delete)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_name_asc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.NAME_ASC)
                true
            }
            R.id.action_sort_name_desc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.NAME_DESC)
                true
            }
            R.id.action_sort_price_asc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.PRICE_ASC)
                true
            }
            R.id.action_sort_price_desc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.PRICE_DESC)
                true
            }
            R.id.action_sort_qty_asc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.QTY_ASC)
                true
            }
            R.id.action_sort_qty_desc -> {
                viewModel.sortItems(InventoryViewModel.SortOption.QTY_DESC)
                true
            }
            R.id.action_refresh -> {
                loadData()
                true
            }
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                prefs.edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
