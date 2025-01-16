package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.AdapterShipper
import com.example.adminfoodordering.databinding.ActivityManageAccountBinding
import com.example.adminfoodordering.model.Shipper
import com.example.adminfoodordering.model.User
import com.example.foododering.Adapter.AdapterUser
import com.google.firebase.database.*

class ManageAccountActivity : AppCompatActivity(), AdapterUser.OnItemClickListener {

    private lateinit var binding: ActivityManageAccountBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userList: ArrayList<User>
    private lateinit var shipperList: ArrayList<Shipper>
    private lateinit var adapterUser: AdapterUser
    private lateinit var adapterShipper: AdapterShipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase reference
        databaseReference = FirebaseDatabase.getInstance().reference
        userList = ArrayList()
        shipperList = ArrayList()

        // Cài đặt RecyclerView
        setupRecyclerViewUsers()
        setupRecyclerViewShippers()

        // Tải dữ liệu từ Firebase
        loadUsersFromFirebase()
        loadShippersFromFirebase()

        // Xử lý nút quay lại
        binding.btnBack4.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViewShippers() {
        // Thiết lập RecyclerView cho Shipper
        binding.RecycleViewShippers.layoutManager = LinearLayoutManager(this)
        adapterShipper = AdapterShipper(this, shipperList)
        binding.RecycleViewShippers.adapter = adapterShipper
    }

    private fun setupRecyclerViewUsers() {
        // Thiết lập RecyclerView cho User
        binding.RecycleViewUsers.layoutManager = LinearLayoutManager(this)
        adapterUser = AdapterUser(this, userList, databaseReference, this)
        binding.RecycleViewUsers.adapter = adapterUser
    }

    private fun loadUsersFromFirebase() {
        databaseReference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        it.uid = userSnapshot.key // Gán UID từ key của node
                        userList.add(it)
                    }
                }
                adapterUser.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageAccountActivity, "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", error.message)
            }
        })
    }

    private fun loadShippersFromFirebase() {
        databaseReference.child("shippers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                shipperList.clear()
                for (shipperSnapshot in snapshot.children) {
                    val shipperId = shipperSnapshot.key ?: continue
                    val name = shipperSnapshot.child("name").getValue(String::class.java) ?: "Không xác định"
                    val phone = shipperSnapshot.child("phone").getValue(String::class.java) ?: "Không xác định"
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        shipperList.add(Shipper(shipperId,name, phone, "0.0", "0.0", ""))
                    }
                }
                adapterShipper.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageAccountActivity, "Lỗi khi tải danh sách shipper", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseError", error.message)
            }
        })
    }

    override fun onItemClick(item: User) {
        // Chuyển sang DetailUserActivity
        val intent = Intent(this, DetailUserActivity::class.java).apply {
            putExtra("userName", item.name)
            putExtra("userEmail", item.email)
            putExtra("userPhone", item.phone)
            putExtra("userAddress", item.address)
        }
        startActivity(intent)
    }
}
