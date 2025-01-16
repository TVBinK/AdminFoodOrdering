package com.example.adminfoodordering

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.AdapterMenu
import com.example.adminfoodordering.model.AllMenu
import com.example.adminfoodordering.databinding.ActivityAllitemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllitemActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var menuItems: ArrayList<AllMenu> = ArrayList()

    private val binding: ActivityAllitemBinding by lazy {
        ActivityAllitemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference
        binding.btnBack2.setOnClickListener {
            finish()
        }
        retrieveMenuItems()
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")
        // Fetch data from database
        foodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.key = foodSnapshot.key // Gắn key vào menuItem
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                setupRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("databaseError", "${error.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        val adapter = AdapterMenu(this@AllitemActivity, menuItems, databaseReference)
        binding.AllitemRecycleView.layoutManager = LinearLayoutManager(this)
        binding.AllitemRecycleView.adapter = adapter
    }
}
