// PendingOrderActivity.kt
package com.example.adminfoodordering

import OrderDetails
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.PendingOrderAdapter
import com.example.adminfoodordering.databinding.ActivityPendingOrderBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendingOrderBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference

    private var listOfOrderItem: MutableList<OrderDetails> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack3.setOnClickListener{
            finish()
        }

        database = FirebaseDatabase.getInstance()
        databaseOrderDetails = database.getReference("OrderDetails")

        getOrderDetails()
    }

    private fun getOrderDetails() {
        databaseOrderDetails.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)
                    order?.let {
                        listOfOrderItem.add(it)
                    }
                }
                setUpAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching data: ${error.message}")
            }
        })
    }

    private fun setUpAdapter() {
        binding.PendingOrdersRecycleView.layoutManager = LinearLayoutManager(this)
        val adapter = PendingOrderAdapter(this, listOfOrderItem) { selectedOrder ->
            val intent = Intent(this, ShowListOrderActivity::class.java)
            intent.putExtra("selectedOrder", selectedOrder)
            startActivity(intent)
            finish()
        }
        binding.PendingOrdersRecycleView.adapter = adapter
    }
}
