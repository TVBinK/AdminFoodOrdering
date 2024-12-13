// PendingOrderAdapter.kt
package com.example.adminfoodordering.Adapter

import OrderDetails
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminfoodordering.databinding.PendingOrderItemBinding

class PendingOrderAdapter(
    private val context: Context,
    private val listOfOrderItem: MutableList<OrderDetails>,
    private val onItemClick: (OrderDetails) -> Unit
) : RecyclerView.Adapter<PendingOrderAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding =
            PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = listOfOrderItem.size

    inner class MenuViewHolder(private val binding: PendingOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val order = listOfOrderItem[position]
            binding.apply {
                tvCustomerName.text = order.userName
                tvQuantity.text = order.foodQuantities?.sum()?.toString() ?: "0"
                tvPrice.text = order.totalPrice

                val uri = order.foodImages?.firstOrNull()?.let { Uri.parse(it) }
                Glide.with(context).load(uri).into(imgViewItem)

                root.setOnClickListener {
                    onItemClick(order)
                }
            }
        }
    }
}
