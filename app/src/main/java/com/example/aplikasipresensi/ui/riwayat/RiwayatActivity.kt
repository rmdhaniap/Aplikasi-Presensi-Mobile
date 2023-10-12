package com.example.aplikasipresensi.ui.riwayat

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.adapter.RecyclerAdapterRiwayat
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.api.ApiService
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RiwayatActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var btnHome: ImageView;
    lateinit var btnBack: Button;
    lateinit var tvDate: TextView
    lateinit var tvDateTime: TextView
    lateinit var btnPrevious: ImageView
    lateinit var btnNext: ImageView
    lateinit var mApiClient: ApiService
    lateinit var loading: ProgressDialog
    lateinit var context: Context
    lateinit var cardDetail : CardView
    lateinit var tvDialogJamKeluar : TextView
    lateinit var tvDialogJamMasuk : TextView
    lateinit var swipeRefreshLayout : SwipeRefreshLayout
    var token: String = ""
    var currentMonth = 0
    var currentYear = 0
    lateinit var dataList: ArrayList<Riwayat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        context = this@RiwayatActivity
        val apiConfig = ApiConfig()
        mApiClient = apiConfig.getApiService()!!

        token = intent.getStringExtra("token").toString()

        btnHome = findViewById(R.id.btn_back_home)
        btnPrevious = findViewById(R.id.btn_previous_date)
        btnNext = findViewById(R.id.btn_next_date)
        tvDate = findViewById(R.id.tv_date)
        tvDateTime = findViewById(R.id.tv_date_time)
        btnBack = findViewById(R.id.btn_back)
        recyclerView = findViewById(R.id.rv_data)
        cardDetail = findViewById(R.id.card_detail)
        tvDialogJamMasuk = findViewById(R.id.tv_dialog_jam_masuk)
        tvDialogJamKeluar = findViewById(R.id.tv_dialog_jam_keluar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        btnHome.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            cardDetail.visibility = View.GONE
        }

        swipeRefreshLayout.setOnRefreshListener {
            val startDate = "$currentYear-$currentMonth-1"
            val endDate = "$currentYear-$currentMonth-${getJumlahHari(currentMonth, currentYear)}"
            riwayat(startDate, endDate, "now")
        }

        btnNext.setOnClickListener {
            currentMonth += 1
            if (currentMonth > 12) {
                currentYear += 1
                currentMonth = 1
            }
            tvDate.text = "${currentMonth.toBulanName()} $currentYear"
            val startDate = "$currentYear-$currentMonth-1"
            val endDate = "$currentYear-$currentMonth-${getJumlahHari(currentMonth, currentYear)}"
            riwayat(startDate, endDate, "next")
        }

        btnPrevious.setOnClickListener {
            currentMonth -= 1
            if (currentMonth == 0) {
                currentYear -= 1
                currentMonth = 12
            }
            tvDate.text = "${currentMonth.toBulanName()} $currentYear"
            val startDate = "$currentYear-$currentMonth-1"
            val endDate = "$currentYear-$currentMonth-${getJumlahHari(currentMonth, currentYear)}"
            riwayat(startDate, endDate, "previous")
        }

        val currentDate = LocalDate.now()
        currentMonth = currentDate.monthValue
        currentYear = currentDate.year
        tvDate.text = "${currentMonth.toBulanName()} $currentYear"

        val startDate = "$currentYear-$currentMonth-1"
        val endDate = "$currentYear-$currentMonth-${getJumlahHari(currentMonth, currentYear)}"
        riwayat(startDate, endDate, "now")
    }

    fun riwayat(startDate: String, endDate: String, arrow: String) {
        dataList = ArrayList()

        for (i in 0 until getJumlahHari(currentMonth, currentYear)) {
            dataList.add(Riwayat())
        }

        loading = ProgressDialog.show(this, null, "Harap Tunggu...", true, false)
        mApiClient.riwayat("Bearer ${token}", startDate, endDate)
            ?.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    swipeRefreshLayout.isRefreshing = false
                    if (response.isSuccessful) {
                        try {
                            val json = JSONObject(response.body().toString())
                            Log.i("JSON DATA", json.toString())

                            val dataArray = json.getJSONArray("data")
                            for (i in 0 until dataArray.length()) {
                                val dataObject = JSONObject(dataArray[i].toString())
                                val createdAt = dataObject.getString("created_at")
                                val tipe = dataObject.getString("tipe")

                                val numberDate = getNumberDate(createdAt)
                                if (tipe == "1") {
                                    dataList[numberDate].jamMasuk = formatTime(createdAt)
                                }
                                else if (tipe == "2") {
                                    dataList[numberDate].jamKeluar = formatTime(createdAt)
                                }
                                dataList[numberDate].tanggal = createdAt
                            }
                            val adapter = RecyclerAdapterRiwayat(dataList, context)
                            recyclerView.adapter = adapter
                            adapter.setOnClickCallback(object : RecyclerAdapterRiwayat.OnItemClickCallback {
                                override fun onItemClick(item: Riwayat?, position: Int) {
                                    cardDetail.visibility = View.VISIBLE
                                    formatDateTime("$currentYear-$currentMonth-${position+1}")
                                    if (item!!.jamMasuk != "") {
                                        tvDialogJamMasuk.text = item.jamMasuk
                                    }
                                    else {
                                        tvDialogJamMasuk.text = "-"
                                    }

                                    if (item.jamKeluar != "") {
                                        tvDialogJamKeluar.text = item.jamKeluar
                                    }
                                    else {
                                        tvDialogJamKeluar.text = "-"
                                    }
                                }
                            })
                            loading.dismiss()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                        loading.dismiss()
                    }
                    else {
                        loading.dismiss()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    swipeRefreshLayout.isRefreshing = false
                    Log.i("debug", "onFailure: ERROR > $t")
                    if (arrow == "next") {
                        currentMonth -= 1
                        if (currentMonth == 0) {
                            currentYear -= 1
                            currentMonth = 12
                        }
                        tvDate.text = "${currentMonth.toBulanName()} $currentYear"
                    }
                    else if (arrow == "previous") {
                        currentMonth += 1
                        if (currentMonth > 12) {
                            currentYear += 1
                            currentMonth = 1
                        }
                        tvDate.text = "${currentMonth.toBulanName()} $currentYear"
                    }
                    Toast.makeText(context, "Get Data Failed", Toast.LENGTH_SHORT).show()
                    loading.dismiss()
                }
            })
    }

    fun formatTime(inputDate: String): String {
        // Parse input string to LocalDateTime
        val dateTime = LocalDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME)

        // Format jam (HH:mm)
        val formattedTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        return formattedTime;
    }

    fun getNumberDate(inputDate: String): Int {
        // Parse input string to LocalDateTime
        val dateTime = LocalDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME)
        return dateTime.dayOfMonth
    }

    fun formatDateTime(inputDate: String) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(inputDate)

        tvDateTime.text = outputFormat.format(date)
    }

    fun getJumlahHari(bulan: Int, tahun: Int): Int {
        val jumlahHari: Int = when (bulan) {
            1, 3, 5, 7, 8, 10, 12 -> 31 // Bulan dengan 31 hari
            4, 6, 9, 11 -> 30 // Bulan dengan 30 hari
            2 -> {
                // Februari - perhitungan untuk tahun kabisat
                if ((tahun % 4 == 0 && tahun % 100 != 0) || (tahun % 400 == 0)) {
                    29
                } else {
                    28
                }
            }
            else -> {
                return 0
            }
        }
        return jumlahHari
    }

    enum class Bulan {
        JANUARI,
        FEBRUARI,
        MARET,
        APRIL,
        MEI,
        JUNI,
        JULI,
        AGUSTUS,
        SEPTEMBER,
        OKTOBER,
        NOVEMBER,
        DESEMBER
    }

    fun Int.toBulanName(): String {
        return when (this) {
            1 -> Bulan.JANUARI.name.toLowerCase().capitalize()
            2 -> Bulan.FEBRUARI.name.toLowerCase().capitalize()
            3 -> Bulan.MARET.name.toLowerCase().capitalize()
            4 -> Bulan.APRIL.name.toLowerCase().capitalize()
            5 -> Bulan.MEI.name.toLowerCase().capitalize()
            6 -> Bulan.JUNI.name.toLowerCase().capitalize()
            7 -> Bulan.JULI.name.toLowerCase().capitalize()
            8 -> Bulan.AGUSTUS.name.toLowerCase().capitalize()
            9 -> Bulan.SEPTEMBER.name.toLowerCase().capitalize()
            10 -> Bulan.OKTOBER.name.toLowerCase().capitalize()
            11 -> Bulan.NOVEMBER.name.toLowerCase().capitalize()
            12 -> Bulan.DESEMBER.name.toLowerCase().capitalize()
            else -> throw IllegalArgumentException("Angka bulan tidak valid")
        }
    }
}