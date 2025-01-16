package com.example.adminfoodordering

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivityEditFoodBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditFoodBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy dữ liệu từ Intent
        val menuKey = intent.getStringExtra("menuKey") ?: return
        val menuName = intent.getStringExtra("menuName")
        val menuPrice = intent.getStringExtra("menuPrice")
        val menuDescription = intent.getStringExtra("menuDescription")

        // Hiển thị dữ liệu cũ vào các trường
        binding.editTextName.setText(menuName)
        binding.editTextPrice.setText(menuPrice)
        binding.editTextDescription.setText(menuDescription)

        // Tham chiếu đến Firebase
        databaseReference = FirebaseDatabase.getInstance().reference.child("menu").child(menuKey)

        // Xử lý khi nhấn nút Apply
        binding.btnApply.setOnClickListener {
            val updatedName = binding.editTextName.text.toString()
            val updatedPrice = binding.editTextPrice.text.toString()
            val updatedDescription = binding.editTextDescription.text.toString()

            // Tạo map chứa dữ liệu cập nhật
            val updatedData = mapOf(
                "foodName" to updatedName,
                "foodPrice" to updatedPrice,
                "foodDescription" to updatedDescription
            )

            // Cập nhật dữ liệu lên Firebase
            databaseReference.updateChildren(updatedData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    finish() // Đóng activity sau khi cập nhật thành công
                } else {
                    Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //back
        binding.btnBack5.setOnClickListener {
            finish()
        }
    }
}
