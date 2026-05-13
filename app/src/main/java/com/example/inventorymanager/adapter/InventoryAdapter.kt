package com.inventory.manager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.databinding.ItemInventoryCardBinding
import com.inventory.manager.model.InventoryItem

/**
 * RecyclerView adapter for displaying inventory items.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class InventoryAdapter(
    private val onItemClick: (InventoryItem) -> Unit,
    private val onEditClick: (InventoryItem) -> Unit,
    private val onDeleteClick: (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(
        private val binding: ItemInventoryCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem) {
            binding.apply {
                // Set item details
                tvItemName.text = item.itemName
                tvCategory.text = item.category
                tvQuantity.text = "Qty: ${item.quantity}"
                tvPrice.text = item.formattedPrice()
                tvSupplier.text = item.supplier

                // Stock badge
                if (item.inStock) {
                    tvStockStatus.text = "In Stock"
                    tvStockStatus.setBackgroundResource(com.inventory.manager.R.drawable.bg_badge_in_stock)
                } else {
                    tvStockStatus.text = "Out of Stock"
                    tvStockStatus.setBackgroundResource(com.inventory.manager.R.drawable.bg_badge_out_of_stock)
                }

                // Category initial icon
                tvCategoryInitial.text = item.category.take(1).uppercase()

                // Low stock warning
                if (item.quantity in 1..5) {
                    tvLowStock.visibility = android.view.View.VISIBLE
                } else {
                    tvLowStock.visibility = android.view.View.GONE
                }

                // Click listeners
                root.setOnClickListener { onItemClick(item) }
                btnEdit.setOnClickListener { onEditClick(item) }
                btnDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }

    /**
     * DiffUtil for efficient RecyclerView updates.
     */
    class DiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem) =
            oldItem == newItem
    }
}
