package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseAdminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Khởi tạo FirebaseAuth và DatabaseReference
        auth = FirebaseAuth.getInstance()
        databaseAdminReference = FirebaseDatabase.getInstance().reference.child("OrderDetails")

        // Xử lý sự kiện click nút Logout
        binding.Logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Xử lý các click card view
        binding.cardViewAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        binding.ViewAllItem.setOnClickListener {
            startActivity(Intent(this, AllitemActivity::class.java))
        }

        binding.cardViewManageOrder.setOnClickListener {
            startActivity(Intent(this, PendingOrderActivity::class.java))
        }

        // Gọi hàm đếm số lượng đơn hàng đã hoàn thành
        countCompletedOrdersAndCalculateTotal()
        binding.cardViewManageAccount.setOnClickListener {
            startActivity(Intent(this, ManageAccountActivity::class.java))
        }
    }

    // Hàm đếm số lượng đơn hàng "Completed" và tính tổng tiền
    private fun countCompletedOrdersAndCalculateTotal() {
        databaseAdminReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var completedCount = 0
                var pendingCount = 0
                var totalMoney = 0.0 // Tổng tiền

                for (orderSnapshot in snapshot.children) {
                    val orderStatus = orderSnapshot.child("orderAccepted").getValue(String::class.java)
                    val orderPriceString = orderSnapshot.child("totalPrice").getValue(String::class.java)

                    // Chuyển đổi giá trị totalPrice từ chuỗi sang Double
                    val orderPrice = if (!orderPriceString.isNullOrEmpty()) {
                        try {
                            // Loại bỏ ký tự "$" và chuyển đổi thành số
                            orderPriceString.replace("$", "").toDouble()
                        } catch (e: Exception) {
                            0.0 // Giá trị mặc định nếu không chuyển đổi được
                        }
                    } else {
                        0.0
                    }

                    // Đếm số lượng đơn hàng "Completed" và cộng tổng tiền
                    if (orderStatus == "Completed") {
                        completedCount++
                        totalMoney += orderPrice
                    }
                    else {
                        pendingCount++
                    }

                }

                // Hiển thị số lượng đơn hàng đã hoàn thành lên TextView
                binding.tvCompleted.text = "$completedCount"
                binding.tvPending.text = "$pendingCount"
                // Hiển thị tổng tiền lên TextView
                binding.tvMoney.text = "${String.format("%.2f", totalMoney)}"
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
                binding.tvCompleted.text = "Error loading data"
                binding.tvMoney.text = "Error calculating revenue"
            }
        })
    }
}
