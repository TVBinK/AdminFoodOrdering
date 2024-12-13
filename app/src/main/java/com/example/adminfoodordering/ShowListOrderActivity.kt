// ShowListOrderActivity.kt
package com.example.foododering

import OrderDetails
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.Adapter.ShowListOrderAdapter
import com.example.adminfoodordering.databinding.ActivityShowListOrderBinding

class ShowListOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowListOrderBinding
    private lateinit var adapter: ShowListOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val selectedOrder = intent.getSerializableExtra("selectedOrder") as? OrderDetails
        val foodNameList = selectedOrder?.foodNames ?: emptyList()
        val foodPriceList = selectedOrder?.foodPrices ?: emptyList()
        val foodImageList = selectedOrder?.foodImages ?: emptyList()
        val foodQuantityList = selectedOrder?.foodQuantities ?: emptyList()

        setUpAdapter(foodNameList, foodPriceList, foodImageList, foodQuantityList)
    }

    private fun setUpAdapter(
        foodNameList: List<String>,
        foodPriceList: List<String>,
        foodImageList: List<String>,
        foodQuantityList: List<Int>
    ) {
        binding.RecycleView.layoutManager = LinearLayoutManager(this)
        adapter = ShowListOrderAdapter(this, foodNameList, foodPriceList, foodImageList, foodQuantityList)
        binding.RecycleView.adapter = adapter
    }
}
