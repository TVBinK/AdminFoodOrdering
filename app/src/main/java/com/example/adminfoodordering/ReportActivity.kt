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

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = ArrayList<OrderDetails>()
    private val salesReport = HashMap<String, Double>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var fromDate: Long = 0
    private var toDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("OrderDetails")

        // Cài đặt RecyclerView
        binding.rvReportResult.layoutManager = LinearLayoutManager(this)

        // Lắng nghe sự kiện chọn ngày
        binding.etFromDate.setOnClickListener { showDatePicker { date ->
            fromDate = date.timeInMillis
            binding.etFromDate.setText(dateFormat.format(date.time))
        } }

        binding.etToDate.setOnClickListener { showDatePicker { date ->
            toDate = date.timeInMillis
            binding.etToDate.setText(dateFormat.format(date.time))
        } }

        binding.btnExportExcel.setOnClickListener {
            when (binding.rgReportType.checkedRadioButtonId) {
                binding.rbReportByFood.id -> exportToExcel(reportList, ReportType.BY_FOOD)
                binding.rbReportByCustomer.id -> exportToExcel(reportList, ReportType.BY_CUSTOMER)
                binding.rbReportBySales.id -> exportSalesToExcel(salesReport)
            }
        }

        // Lắng nghe sự kiện tạo báo cáo
        binding.btnGenerateReport.setOnClickListener {
            if (fromDate == 0L || toDate == 0L) {
                Toast.makeText(this, "Vui lòng chọn ngày hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra loại báo cáo: theo món ăn, khách hàng, hoặc doanh số theo ngày
            when (binding.rgReportType.checkedRadioButtonId) {
                binding.rbReportByFood.id -> generateReport(fromDate, toDate, ReportType.BY_FOOD)
                binding.rbReportByCustomer.id -> generateReport(fromDate, toDate, ReportType.BY_CUSTOMER)
                binding.rbReportBySales.id -> generateSalesReport(fromDate, toDate)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun generateReport(fromDate: Long, toDate: Long, reportType: ReportType) {
        binding.progressBar.visibility = View.VISIBLE
        reportList.clear()

        databaseReference
            .orderByChild("currentTime")
            .startAt(fromDate.toDouble())
            .endAt(toDate.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)
                        if (order != null) {
                            reportList.add(order)
                        }
                    }

                    reportAdapter = ReportAdapter(this@ReportActivity, reportList, reportType)
                    binding.rvReportResult.adapter = reportAdapter

                    binding.progressBar.visibility = View.GONE

                    if (reportList.isEmpty()) {
                        Toast.makeText(this@ReportActivity, "Không có dữ liệu trong khoảng thời gian này!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ReportActivity, "Lỗi khi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
                        if (order != null) {
                            val orderDate = dateFormat.format(Date(order.currentTime))
                            val orderTotal = order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0
                            salesReport[orderDate] = (salesReport[orderDate] ?: 0.0) + orderTotal
                        }
                    }

                    reportAdapter = ReportAdapter(this@ReportActivity, salesReport)
                    binding.rvReportResult.adapter = reportAdapter

                    binding.progressBar.visibility = View.GONE

                    if (salesReport.isEmpty()) {
                        Toast.makeText(this@ReportActivity, "Không có dữ liệu doanh số trong khoảng thời gian này!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ReportActivity, "Lỗi khi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun exportToExcel(reportList: List<OrderDetails>, reportType: ReportType) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Report")

            // Tạo dòng tiêu đề
            val headerRow = sheet.createRow(0)
            val headers = when (reportType) {
                ReportType.BY_FOOD -> listOf("Tên món", "Tổng tiền")
                ReportType.BY_CUSTOMER -> listOf("Tên khách hàng", "Tổng tiền")
                else -> emptyList()
            }

            for ((index, title) in headers.withIndex()) {
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
            }

            // Điền dữ liệu
            for (rowIndex in reportList.indices) {
                val row = sheet.createRow(rowIndex + 1)
                val order = reportList[rowIndex]
                when (reportType) {
                    ReportType.BY_FOOD -> {
                        val foodNames = order.foodNames?.joinToString(", ") ?: "Không rõ"
                        val totalPrice = order.foodPrices?.map { it.replace("$", "").toDoubleOrNull() ?: 0.0 }?.sum() ?: 0.0

                        row.createCell(0).setCellValue(foodNames)
                        row.createCell(1).setCellValue(totalPrice)
                    }

                    ReportType.BY_CUSTOMER -> {
                        val userName = order.userName ?: "Không rõ"
                        val totalPrice = order.totalPrice?.replace("$", "")?.toDoubleOrNull() ?: 0.0

                        row.createCell(0).setCellValue(userName)
                        row.createCell(1).setCellValue(totalPrice)
                    }

                    else -> {}
                }
            }

            saveExcelFile(workbook, "Report_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi xuất Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportSalesToExcel(salesReport: Map<String, Double>) {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Sales Report")

            // Tạo dòng tiêu đề
            val headerRow = sheet.createRow(0)
            val headers = listOf("Ngày", "Doanh số")
            for ((index, title) in headers.withIndex()) {
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
            }

            // Điền dữ liệu
            salesReport.forEach { (date, totalSales) ->
                val rowIndex = salesReport.entries.indexOfFirst { it.key == date } + 1
                val row = sheet.createRow(rowIndex)
                row.createCell(0).setCellValue(date)
                row.createCell(1).setCellValue(totalSales)
            }

            saveExcelFile(workbook, "SalesReport_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi xuất Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveExcelFile(workbook: XSSFWorkbook, fileName: String) {
        try {
            // Lưu file vào thư mục con của ứng dụng trên bộ nhớ ngoài
            val foodExcelsDir = File(getExternalFilesDir(null), "FoodExcels")

            // Nếu thư mục không tồn tại, tạo thư mục
            if (!foodExcelsDir.exists()) {
                foodExcelsDir.mkdirs()
            }

            // Tạo file Excel trong thư mục "FoodExcels"
            val file = File(foodExcelsDir, fileName)
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

            Toast.makeText(this, "Đã lưu file Excel: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            Log.d("ExcelExport", "Đã lưu file Excel: ${file.absolutePath}")
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi lưu file Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}
