package com.example.adminfoodordering.adapter

import android.content.Context
import android.net.Uri
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminfoodordering.model.AllMenu
import com.example.adminfoodordering.databinding.MenuItemBinding
import com.google.firebase.database.DatabaseReference

class AdapterMenu(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    private val databaseReference: DatabaseReference
) : RecyclerView.Adapter<AdapterMenu.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val menuItem = menuList[position]
            binding.apply {
                val uriString = menuItem.foodImage
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(imgViewItem)

                tvFoodName.text = menuItem.foodName
                tvPrice.text = menuItem.foodPrice

                // Xóa item khi nhấn nút Delete
                btnDelete.setOnClickListener {
                    val menuKey = menuItem.key
                    if (menuKey != null) {
                        databaseReference.child("menu").child(menuKey).removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Xóa thành công, cập nhật RecyclerView
                                    menuList.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, menuList.size)
                                    Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Xóa không thành công!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Không thể xóa item này!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
