package com.example.adminfoodordering

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivityDetailUserBinding

class DetailUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailUserBinding  // Khai báo binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng ViewBinding để liên kết layout với Activity
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu từ Intent
        val userName = intent.getStringExtra("userName")
        val userAddress = intent.getStringExtra("userAddress")
        val userPhone = intent.getStringExtra("userPhone")
        val userEmail = intent.getStringExtra("userEmail")

        // Đặt giá trị vào các EditText thông qua binding
        binding.editTextName.setText(userName)
        binding.editTextAddress.setText(userAddress)
        binding.editTextPhone.setText(userPhone)
        binding.editTextEmail.setText(userEmail)

        // Cài đặt sự kiện cho nút back
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
