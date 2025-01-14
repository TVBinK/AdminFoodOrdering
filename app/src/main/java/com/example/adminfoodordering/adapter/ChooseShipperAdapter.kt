package com.example.adminfoodordering.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminfoodordering.databinding.ShipperItemBinding
import com.example.adminfoodordering.model.Shipper

class ChooseShipperAdapter(
    private val shippers: MutableList<Shipper>,
    private val onItemClick: (Shipper) -> Unit
) : RecyclerView.Adapter<ChooseShipperAdapter.ShipperViewHolder>() {

    inner class ShipperViewHolder(private val binding: ShipperItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shipper: Shipper) {
            binding.tvName.text = shipper.name
            binding.tvPhone.text = shipper.phone
            binding.root.setOnClickListener { onItemClick(shipper) }
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

    /**
     * Update the list of shippers and refresh the RecyclerView.
     */
    fun updateData(newShippers: List<Shipper>) {
        shippers.clear()
        shippers.addAll(newShippers)
        notifyDataSetChanged()
    }
}
