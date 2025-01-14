package com.example.adminfoodordering.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminfoodordering.databinding.ShipperItemBinding
import com.example.adminfoodordering.model.Shipper

class AdapterShipper(
    private val context: android.content.Context,
    private val shippers: MutableList<Shipper>,
) : RecyclerView.Adapter<AdapterShipper.ShipperViewHolder>() {

    inner class ShipperViewHolder(private val binding: ShipperItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shipper: Shipper) {
            binding.tvName.text = shipper.name
            binding.tvPhone.text = shipper.phone
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipperViewHolder {
        val binding = ShipperItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ShipperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShipperViewHolder, position: Int) {
        holder.bind(shippers[position])
    }

    override fun getItemCount(): Int = shippers.size

    fun updateData(newShippers: List<Shipper>) {
        shippers.clear()
        shippers.addAll(newShippers)
        notifyDataSetChanged()
    }
}
