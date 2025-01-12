package com.example.adminfoodordering.adapter

import OrderDetails
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminfoodordering.databinding.ReportItemBinding
import com.example.adminfoodordering.model.ReportType

// ReportAdapter.kt

class ReportAdapter(
    private val context: Context,
    private val foodCountMap: Map<String, Int>? = null,
    private val reportList: ArrayList<OrderDetails> = ArrayList(),
    private val salesReport: Map<String, Double>? = null,
    private val reportType: ReportType = ReportType.BY_FOOD
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return when (reportType) {
            ReportType.BY_FOOD -> foodCountMap?.size ?: 0
            ReportType.BY_SALES -> salesReport?.size ?: 0
            ReportType.BY_CUSTOMER -> reportList.size
        }
    }

    inner class ReportViewHolder(private val binding: ReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            when (reportType) {
                ReportType.BY_FOOD -> bindFoodReport(position)
                ReportType.BY_CUSTOMER -> bindCustomerReport(position)
                ReportType.BY_SALES -> bindSalesReport(position)
            }
        }

        private fun bindFoodReport(position: Int) {
            foodCountMap?.let { map ->
                val foodName = map.keys.elementAt(position)
                val count = map[foodName] ?: 0
                binding.tvReportItem.text = "$foodName: $count phần"
            }
        }

        private fun bindCustomerReport(position: Int) {
            val order = reportList[position]
            val userName = order.userName ?: "Không rõ"
            val totalPrice = order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0

            binding.tvReportItem.text = buildString {
                appendLine("Khách hàng: $userName")
                append("Tổng tiền: $${formatCurrency(totalPrice)}")
            }
        }

        private fun bindSalesReport(position: Int) {
            salesReport?.let { report ->
                val date = report.keys.elementAt(position)
                val amount = report[date] ?: 0.0

                binding.tvReportItem.text = buildString {
                    appendLine("Ngày: $date")
                    append("Doanh số: $${formatCurrency(amount)}")
                }
            }
        }

        private fun formatCurrency(amount: Double): String = "%.2f".format(amount)
    }
}