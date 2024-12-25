package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.databinding.ActivityManageAccountBinding
import com.example.adminfoodordering.model.User
import com.example.foododering.Adapter.AdapterUser
import com.google.firebase.database.*

class ManageAccountActivity : AppCompatActivity(), AdapterUser.OnItemClickListener {

    private lateinit var binding: ActivityManageAccountBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userList: ArrayList<User>
    private lateinit var adapterUser: AdapterUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        userList = ArrayList()

        // Cài đặt RecyclerView
        setupRecyclerView()

        // Tải danh sách người dùng từ Firebase
        loadUsersByUID()
        binding.btnBack4.setOnClickListener{
            finish()
        }
    }

    private fun setupRecyclerView() {
        // Thiết lập layout manager theo chiều ngang
        adapterUser = AdapterUser(this, userList, databaseReference, this)
        binding.RecycleViewUsers.layoutManager = LinearLayoutManager(this)
        binding.RecycleViewUsers.adapter = adapterUser
    }

    private fun loadUsersByUID() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        it.uid = userSnapshot.key // Gán UID từ key của node trong Firebase
                        userList.add(it)
                    }
                }
                adapterUser.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageAccountActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
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
