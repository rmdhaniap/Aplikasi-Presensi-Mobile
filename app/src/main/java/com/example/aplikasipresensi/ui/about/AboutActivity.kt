package com.example.aplikasipresensi.ui.about

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.api.ApiService
import com.example.aplikasipresensi.data.response.InformasiUmum
import com.example.aplikasipresensi.databinding.ActivityAboutBinding
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackHome.setOnClickListener {
            finish()
        }

        val apiBaseUrl = "http://103.100.27.59/"
        val retrofit = Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getInformasiUmum()

        call.enqueue(object : Callback<InformasiUmum> {
            override fun onResponse(call: Call<InformasiUmum>, response: Response<InformasiUmum>) {
                if (response.isSuccessful) {
                    val informasiUmum = response.body()
                    if (informasiUmum != null) {
                        val namaAplikasiTextView = findViewById<TextView>(R.id.namaAplikasiTextView)
                        val deskripsiTextView = findViewById<TextView>(R.id.deskripsiTextView)
                        val daerahTextView = findViewById<TextView>(R.id.daerahTextView)
                        val logoImageView = findViewById<ImageView>(R.id.logoImageView)

                        namaAplikasiTextView.text = informasiUmum.nama_aplikasi
                        deskripsiTextView.text = informasiUmum.deskripsi
                        daerahTextView.text = informasiUmum.daerah

                        Picasso.get().load(informasiUmum.logo).into(logoImageView)
                    }
                }
            }

            override fun onFailure(call: Call<InformasiUmum>, t: Throwable) {
                Log.e(TAG, "onFilure: $t")
            }
        })
    }
}