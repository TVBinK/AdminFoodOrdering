package com.example.adminfoodordering.adapter

import OrderDetails
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminfoodordering.databinding.ReportItemBinding
import com.example.adminfoodordering.model.ReportType

class ReportAdapter(
    private val context: Context,
    private val reportList: ArrayList<OrderDetails>,
    private val reportType: ReportType
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private var salesReport: Map<String, Double>? = null

    constructor(context: Context, salesReport: Map<String, Double>) : this(
        context,
        ArrayList(),
        ReportType.BY_SALES
    ) {
        this.salesReport = salesReport
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return if (reportType == ReportType.BY_SALES) {
            salesReport?.size ?: 0
        } else {
            reportList.size
        }
    }

    inner class ReportViewHolder(private val binding: ReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            when (reportType) {
                ReportType.BY_FOOD -> loadFoodData(reportList[position], binding)
                ReportType.BY_CUSTOMER -> loadCustomerData(reportList[position], binding)
                ReportType.BY_SALES -> loadSalesData(position, binding)
            }
        }

        private fun loadFoodData(order: OrderDetails, binding: ReportItemBinding) {
            val foodNames = order.foodNames ?: return
            val totalPrice =
                order.foodPrices?.map { it.replace("$", "").toDoubleOrNull() ?: 0.0 }?.sum() ?: 0.0

            binding.tvReportItem.text = """
                Món ăn: ${foodNames.joinToString(", ")}
                Tổng tiền: $${"%.2f".format(totalPrice)}
            """.trimIndent()
        }

        private fun loadCustomerData(order: OrderDetails, binding: ReportItemBinding) {
            val userName = order.userName ?: "Không rõ"
            val totalPrice = order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0

            binding.tvReportItem.text = """
                Khách hàng: $userName
                Tổng tiền: $${"%.2f".format(totalPrice)}
            """.trimIndent()
        }

        private fun loadSalesData(position: Int, binding: ReportItemBinding) {
            val salesDate = salesReport!!.keys.toList()[position]
            val totalSales = salesReport!![salesDate] ?: 0.0

            binding.tvReportItem.text = """
                Ngày: $salesDate
                Doanh số: $${"%.2f".format(totalSales)}
            """.trimIndent()
        }
    }
}
