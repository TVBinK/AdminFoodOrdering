package com.example.adminfoodordering

import OrderDetails
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminfoodordering.adapter.ReportAdapter
import com.example.adminfoodordering.databinding.ActivityReportBinding
import com.example.adminfoodordering.model.ReportType
import com.google.firebase.database.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.apache.poi.ss.usermodel.Sheet

// ReportActivity.kt

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = ArrayList<OrderDetails>()
    private val salesReport = HashMap<String, Double>()
    private val foodCountMap = HashMap<String, Int>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var fromDate: Long = 0
    private var toDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        databaseReference = FirebaseDatabase.getInstance().getReference("OrderDetails")
        binding.rvReportResult.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.etFromDate.setOnClickListener { showDatePicker { date ->
            fromDate = date.timeInMillis
            binding.etFromDate.setText(dateFormat.format(date.time))
        } }

        binding.etToDate.setOnClickListener { showDatePicker { date ->
            toDate = date.timeInMillis
            binding.etToDate.setText(dateFormat.format(date.time))
        } }

        binding.btnGenerateReport.setOnClickListener {
            if (validateDates()) {
                generateSelectedReport()
            }
        }

        binding.btnExportExcel.setOnClickListener {
            when (binding.rgReportType.checkedRadioButtonId) {
                binding.rbReportByFood.id -> exportToExcel(reportList, ReportType.BY_FOOD)
                binding.rbReportByCustomer.id -> exportToExcel(reportList, ReportType.BY_CUSTOMER)
                binding.rbReportBySales.id -> exportSalesToExcel(salesReport)
            }
        }
    }

    private fun validateDates(): Boolean {
        if (fromDate == 0L || toDate == 0L) {
            Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun generateSelectedReport() {
        when (binding.rgReportType.checkedRadioButtonId) {
            binding.rbReportByFood.id -> generateReport(fromDate, toDate, ReportType.BY_FOOD)
            binding.rbReportByCustomer.id -> generateReport(fromDate, toDate, ReportType.BY_CUSTOMER)
            binding.rbReportBySales.id -> generateSalesReport(fromDate, toDate)
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun generateReport(fromDate: Long, toDate: Long, reportType: ReportType) {
        binding.progressBar.visibility = View.VISIBLE
        reportList.clear()
        foodCountMap.clear()

        databaseReference
            .orderByChild("currentTime")
            .startAt(fromDate.toDouble())
            .endAt(toDate.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)
                        order?.let {
                            reportList.add(it)
                            if (reportType == ReportType.BY_FOOD) {
                                countFoodItems(it)
                            }
                        }
                    }

                    updateAdapter(reportType)
                    binding.progressBar.visibility = View.GONE

                    if (reportList.isEmpty()) {
                        showNoDataToast()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    handleDatabaseError(error)
                }
            })
    }

    private fun countFoodItems(order: OrderDetails) {
        order.foodNames?.forEach { foodName ->
            foodCountMap[foodName] = (foodCountMap[foodName] ?: 0) + 1
        }
    }

    private fun generateSalesReport(fromDate: Long, toDate: Long) {
        binding.progressBar.visibility = View.VISIBLE
        salesReport.clear()

        databaseReference
            .orderByChild("currentTime")
            .startAt(fromDate.toDouble())
            .endAt(toDate.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)
                        order?.let { processOrderForSalesReport(it) }
                    }

                    updateAdapter(ReportType.BY_SALES)
                    binding.progressBar.visibility = View.GONE

                    if (salesReport.isEmpty()) {
                        showNoDataToast()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    handleDatabaseError(error)
                }
            })
    }

    private fun processOrderForSalesReport(order: OrderDetails) {
        val orderDate = dateFormat.format(Date(order.currentTime))
        val orderTotal = order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0
        salesReport[orderDate] = (salesReport[orderDate] ?: 0.0) + orderTotal
    }

    private fun updateAdapter(reportType: ReportType) {
        reportAdapter = when (reportType) {
            ReportType.BY_FOOD -> ReportAdapter(this, foodCountMap = foodCountMap)
            ReportType.BY_CUSTOMER -> ReportAdapter(this, reportList = reportList, reportType = reportType)
            ReportType.BY_SALES -> ReportAdapter(this, salesReport = salesReport, reportType = reportType)
        }
        binding.rvReportResult.adapter = reportAdapter
    }

    private fun showNoDataToast() {
        Toast.makeText(this, "Không có dữ liệu trong khoảng thời gian này!", Toast.LENGTH_SHORT).show()
    }

    private fun handleDatabaseError(error: DatabaseError) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
    }

    private fun exportToExcel(reportList: List<OrderDetails>, reportType: ReportType) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Report")

            createHeaderRow(sheet, reportType)
            fillDataRows(sheet, reportList, reportType)

            saveExcelFile(workbook)
        } catch (e: Exception) {
            handleExcelError(e)
        }
    }

    private fun exportSalesToExcel(salesReport: Map<String, Double>) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Sales Report")

            createSalesHeaderRow(sheet)
            fillSalesDataRows(sheet, salesReport)

            saveExcelFile(workbook)
        } catch (e: Exception) {
            handleExcelError(e)
        }
    }

    private fun createHeaderRow(sheet: Sheet, reportType: ReportType) {
        val headers = when (reportType) {
            ReportType.BY_FOOD -> listOf("Tên món", "Số lượng")
            ReportType.BY_CUSTOMER -> listOf("Tên khách hàng", "Tổng tiền")
            else -> listOf()
        }

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }
    }

    private fun createSalesHeaderRow(sheet: Sheet) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Ngày")
        headerRow.createCell(1).setCellValue("Doanh số")
    }

    private fun fillDataRows(sheet: Sheet, reportList: List<OrderDetails>, reportType: ReportType) {
        reportList.forEachIndexed { index, order ->
            val row = sheet.createRow(index + 1)
            when (reportType) {
                ReportType.BY_FOOD -> {
                    row.createCell(0).setCellValue(order.foodNames?.joinToString(", ") ?: "")
                    row.createCell(1).setCellValue(
                        order.foodPrices?.size?.toString() ?: "0"
                    )
                }
                ReportType.BY_CUSTOMER -> {
                    row.createCell(0).setCellValue(order.userName ?: "Unknown")
                    row.createCell(1).setCellValue(
                        order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0
                    )
                }
                else -> {}
            }
        }
    }

    private fun fillSalesDataRows(sheet: Sheet, salesReport: Map<String, Double>) {
        salesReport.entries.forEachIndexed { index, (date, amount) ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(date)
            row.createCell(1).setCellValue(amount)
        }
    }

    private fun saveExcelFile(workbook: XSSFWorkbook) {
        val foodExcelsDir = File(getExternalFilesDir(null), "FoodExcels").apply {
            if (!exists()) mkdirs()
        }

        val fileName = "Report_${System.currentTimeMillis()}.xlsx"
        val file = File(foodExcelsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
        }

        Toast.makeText(this, "Đã lưu file Excel: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        Log.d("ExcelExport", "Đã lưu file Excel: ${file.absolutePath}")
    }

    private fun handleExcelError(e: Exception) {
        Toast.makeText(this, "Lỗi khi xuất Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}