package com.example.foododering.Adapter

import OrderDetails
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.adminfoodordering.databinding.AccountItemBinding
import com.example.adminfoodordering.model.User
import com.google.firebase.database.*


class AdapterUser(
    private val context: Context,
    private val menuList: ArrayList<User>,
    private val databaseReference: DatabaseReference,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdapterUser.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = AccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    interface OnItemClickListener {
        fun onItemClick(item: User)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: AccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val user = menuList[position]

            // Cập nhật tên người dùng
            binding.tvName.text = user.name

            // Tải và tính tổng tiền từ Firebase và số đơn hàng
            loadOrderData(user.uid, binding)

            // Set sự kiện click cho item
            itemView.setOnClickListener {
                listener.onItemClick(user)
            }
        }

        private fun loadOrderData(userUid: String?, binding: AccountItemBinding) {
            // Nếu UID không có, không tính tổng tiền
            if (userUid == null) return

            // Lấy dữ liệu đơn hàng của người dùng từ Firebase
            val ordersRef = FirebaseDatabase.getInstance().getReference("users").child(userUid)
                .child("OrderDetails")
            ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalPrice = 0.0
                    var totalOrders = 0

                    // Đếm số lượng đơn hàng và tính tổng tiền với điều kiện trạng thái
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)

                        // Kiểm tra trạng thái orderAccepted == "Completed"
                        if (order?.orderAccepted == "Completed") {
                            order.totalPrice?.let {
                                // Chuyển đổi giá trị totalPrice từ String sang Double và cộng dồn
                                val price = it.replace("$", "").toDoubleOrNull() ?: 0.0
                                totalPrice += price
                            }
                            totalOrders++ // Tăng tổng số đơn hàng có trạng thái Completed
                        }
                    }

                    // Hiển thị tổng tiền và tổng số đơn hàng
                    binding.tvPrice.text = "$${"%.2f".format(totalPrice)}"
                    binding.tvCount.text = "$totalOrders" // Hiển thị tổng số đơn hàng Completed
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error loading orders", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }
}
