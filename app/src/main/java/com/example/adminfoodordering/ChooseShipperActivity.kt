package com.example.adminfoodordering

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.ChooseShipperAdapter
import com.example.adminfoodordering.databinding.ActivityChooseShipperBinding
import com.example.adminfoodordering.model.Shipper
import com.google.firebase.database.*

class ChooseShipperActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseShipperBinding
    private lateinit var adapter: ChooseShipperAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var orderId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseShipperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy orderId từ Intent
        orderId = intent.getStringExtra("ORDER_ID") ?: ""
        //Lấy UserID từ Intent
        userId = intent.getStringExtra("USER_ID") ?: ""

        // Khởi tạo tham chiếu Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("shippers")

        // Cài đặt RecyclerView
        binding.rvChooseShipper.layoutManager = LinearLayoutManager(this)
        adapter = ChooseShipperAdapter(mutableListOf()) { shipper ->
            onShipperSelected(shipper)
        }
        binding.rvChooseShipper.adapter = adapter

        // Tải danh sách shipper từ Firebase
        loadShippersFromFirebase()
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadShippersFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shippers = mutableListOf<Shipper>()
                for (shipperSnapshot in snapshot.children) {
                    val shipperId = shipperSnapshot.key ?: continue
                    val name = shipperSnapshot.child("name").getValue(String::class.java) ?: "Không xác định"
                    val phone = shipperSnapshot.child("phone").getValue(String::class.java) ?: "Không xác định"
                    shippers.add(Shipper(shipperId,name, phone, "0.0", "0.0", ""))
                }
                adapter.updateData(shippers)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChooseShipperActivity, "Không thể tải danh sách shipper", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onShipperSelected(shipper: Shipper) {
        // Hiển thị popup xác nhận
        showConfirmationDialog(shipper)
    }

    private fun showConfirmationDialog(shipper: Shipper) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận giao đơn")
            .setMessage("Bạn có chắc muốn giao đơn này cho shipper ${shipper.name}?")
            .setPositiveButton("Đồng ý") { _, _ ->
                // Gán shipper cho đơn hàng sau khi xác nhận
                assignShipperToOrder(orderId, shipper)
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                // Đóng popup nếu người dùng chọn "Hủy"
                dialog.dismiss()
            }
            .show()
    }

    private fun assignShipperToOrder(orderId: String, shipper: Shipper) {
        val orderRefAdmin = FirebaseDatabase.getInstance().getReference("OrderDetails").child(orderId)
        val orderRefUser = FirebaseDatabase.getInstance().getReference("users").child(userId).child("OrderDetails").child(orderId)
        // Cập nhật thông tin shipper cho đơn hàng
        val shipperData = mapOf(
            "shipperID" to shipper.shipperID,
            "shipperName" to shipper.name,
            "shipperPhone" to shipper.phone
        )

        orderRefAdmin.updateChildren(shipperData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Đã giao đơn thành công cho shipper", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Không thể giao đơn", Toast.LENGTH_SHORT).show()
            }
        }
        orderRefUser.updateChildren(shipperData)
    }

}
