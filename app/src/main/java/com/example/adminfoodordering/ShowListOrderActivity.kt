package com.example.foododering

import OrderDetails
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.Adapter.ShowListOrderAdapter
import com.example.adminfoodordering.PendingOrderActivity
import com.example.adminfoodordering.databinding.ActivityShowListOrderBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ShowListOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowListOrderBinding
    private lateinit var adapter: ShowListOrderAdapter
    private lateinit var databaseUserReference: DatabaseReference
    private lateinit var databaseAdminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase references
        databaseUserReference = FirebaseDatabase.getInstance().reference
        databaseAdminReference = FirebaseDatabase.getInstance().reference

        // Back button functionality
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Get the selected order passed from the previous activity
        val selectedOrder = intent.getSerializableExtra("selectedOrder") as? OrderDetails

        // Accept button
        binding.btnAccept.setOnClickListener {
            showConfirmationDialog(
                "Accept Order",
                "Are you sure you want to accept this order?",
                onConfirm = { updateOrderStatus(selectedOrder, "Accepted") }
            )
        }

        // Reject button
        binding.btnReject.setOnClickListener {
            showConfirmationDialog(
                "Reject Order",
                "Are you sure you want to reject this order?",
                onConfirm = { updateOrderStatus(selectedOrder, "Rejected") }
            )
        }

        // Display customer information
        binding.tvName.text = selectedOrder?.userName ?: "N/A"
        binding.tvAddress.text = selectedOrder?.address ?: "N/A"
        binding.tvPhone.text = selectedOrder?.phoneNumber ?: "N/A"
        binding.tvTotal.text = selectedOrder?.totalPrice?.toString() ?: "0.00"

        // Get food details for displaying in the list
        val foodNameList = selectedOrder?.foodNames ?: emptyList()
        val foodPriceList = selectedOrder?.foodPrices ?: emptyList()
        val foodImageList = selectedOrder?.foodImages ?: emptyList()
        val foodQuantityList = selectedOrder?.foodQuantities ?: emptyList()

        // Set up the adapter to display food items
        setUpAdapter(foodNameList, foodPriceList, foodImageList, foodQuantityList)
    }

    // Function to show confirmation dialog
    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Function to update the order status
    private fun updateOrderStatus(selectedOrder: OrderDetails?, status: String) {
        val userIds = selectedOrder?.userUid
        val pushKey = selectedOrder?.itemPushKey

        if (userIds != null && pushKey != null) {
            // Update the order status for the user
            val buyHistoryRef =
                databaseUserReference.child("users").child(userIds).child("OrderDetails").child(pushKey)
            buyHistoryRef.child("orderAccepted").setValue(status)

            // Update the order status for the admin
            databaseAdminReference.child("OrderDetails").child(pushKey).child("orderAccepted").setValue(status)

            Toast.makeText(this, "$status Order successfully", Toast.LENGTH_SHORT).show()

            // Navigate to PendingOrderActivity
            val intent = Intent(this, PendingOrderActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        } else {
            Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show()
        }
    }

    // Set up the adapter to display the list of food items
    private fun setUpAdapter(
        foodNameList: List<String>,
        foodPriceList: List<String>,
        foodImageList: List<String>,
        foodQuantityList: List<Int>
    ) {
        binding.RecycleView.layoutManager = LinearLayoutManager(this)
        adapter = ShowListOrderAdapter(
            this,
            foodNameList,
            foodPriceList,
            foodImageList,
            foodQuantityList
        )
        binding.RecycleView.adapter = adapter
    }
}
