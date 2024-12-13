package com.example.adminfoodordering

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminfoodordering.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        //khởi tạo firebase
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContentView(binding.root)
        //click logout
        binding.Logout.setOnClickListener {
            auth.signOut()
            // Create an intent to launch the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Finish the current activity
            finish()
        }
        //click manage user
        binding.cardViewAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
        //click ViewALlMenu
        binding.ViewAllItem.setOnClickListener {
            startActivity(Intent(this, AllitemActivity::class.java))
        }
        binding.cardViewManageOrder.setOnClickListener {
            //intent den PendingOrderActivity
            startActivity(Intent(this, PendingOrderActivity::class.java))
        }
    }
}