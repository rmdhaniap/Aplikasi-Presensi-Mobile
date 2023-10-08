package com.example.aplikasipresensi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.aplikasipresensi.databinding.ActivityMainBinding
import com.example.aplikasipresensi.ui.presensi.PresensiKeluarActivity
import com.example.aplikasipresensi.ui.presensi.PresensiMasukActivity
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val token = intent.getStringExtra("token")

        binding.namaUser.text = "$name"

//        val jsonResponse = """
//            {
//                "id": 1,
//                "nama_aplikasi": "Aplikasi Presensi",
//                "nama_singkat_aplikasi": "Presensi",
//                "logo": "http:\/\/103.65.214.250:8211\/storage\/gambar\/logo\/e7de10be-f01a-4b96-9921-59376b3b9f63.png",
//                "daerah": "Global Intermedia",
//                "deskripsi": "Aplikasi untuk presensi",
//                "warna": "#ffffff",
//                "koordinat": "-7.813111492789422, 110.37669583357997",
//                "today": "Rabu, 4 Oktober 2023"
//            }
//        """

//        val gson = Gson()
//        val responseObject = gson.fromJson(jsonResponse, ResponseObject::class.java)
//
//        val today = responseObject.today
//
//        binding.txtJam.text = "${getCurrentTime()}"
//        binding.txtDate.text = "${getCurrentDate()}"

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val date = Date()
                val clockFormat = SimpleDateFormat("HH:mm:ss")
                val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy")

                binding.txtJam.setText(clockFormat.format(date))
                binding.txtDate.setText(dateFormat.format(date))

                handler.postDelayed(this, 1000)
            }
        })

//        if (intent.extras != null) {
//            binding.namaUser.setText(intent.getStringExtra("name"))
//        }

        binding.cardMasuk.setOnClickListener {
            intent = Intent(this@MainActivity, PresensiMasukActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        binding.cardKeluar.setOnClickListener {
            intent = Intent(this@MainActivity, PresensiKeluarActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun getCurrentTime(): String {
//        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
//        return currentTime
//    }
//
//    private fun getCurrentDate(): String {
//        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
//        return currentDate.toString()
//    }
//
//    data class ResponseObject(
//        val id: Int,
//        val nama_aplikasi: String,
//        val nama_singkat_aplikasi: String,
//        val logo: String,
//        val daerah: String,
//        val deskripsi: String,
//        val warna: String,
//        val koordinat: String,
//        val today: String
//    )
}