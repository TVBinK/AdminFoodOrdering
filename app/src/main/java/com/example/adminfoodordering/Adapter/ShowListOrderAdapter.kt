// ShowListOrderAdapter.kt
package com.example.adminfoodordering.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminfoodordering.databinding.ListOrderItemBinding

class ShowListOrderAdapter(
    private val context: Context,
    private val foodNameList: List<String>,
    private val foodPriceList: List<String>,
    private val foodImageList: List<String>,
    private val foodQuantityList: List<Int>
) : RecyclerView.Adapter<ShowListOrderAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding =
            ListOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodNameList.size

    inner class MenuViewHolder(private val binding: ListOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                tvFoodName.text = foodNameList[position]
                tvPrice.text = foodPriceList[position]
                FoodQuantity.text = foodQuantityList[position].toString()

                val uri = Uri.parse(foodImageList[position])
                Glide.with(context)
                    .load(uri)
                    .into(imgViewItem)
            }
        }
    }
}
