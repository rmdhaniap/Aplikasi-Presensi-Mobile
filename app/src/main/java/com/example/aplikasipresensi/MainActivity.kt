package com.example.aplikasipresensi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aplikasipresensi.databinding.ActivityMainBinding
import com.example.aplikasipresensi.ui.about.AboutActivity
import com.example.aplikasipresensi.ui.login.LoginActivity
import com.example.aplikasipresensi.ui.presensi.PresensiKeluarActivity
import com.example.aplikasipresensi.ui.presensi.PresensiMasukActivity
import com.example.aplikasipresensi.ui.riwayat.RiwayatActivity
import java.text.SimpleDateFormat
import java.util.Date

private val USERNAME_KEY = stringPreferencesKey("username")
private val TOKEN_KEY = stringPreferencesKey("token")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = ""
            preferences[TOKEN_KEY] = ""
        }

        binding.cardAbout.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val token = intent.getStringExtra("token")

        binding.namaUser.text = "$name"

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

        binding.cardMasuk.setOnClickListener {
            intent = Intent(this@MainActivity, PresensiMasukActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("tipe", "1")
            startActivity(intent)
        }

        binding.cardKeluar.setOnClickListener {
            intent = Intent(this@MainActivity, PresensiKeluarActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("tipe", "2")
            startActivity(intent)
        }

        binding.cardRiwayat.setOnClickListener {
            val intent = Intent(this@MainActivity, RiwayatActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        binding.cardAbout.setOnClickListener {
            intent = Intent(this@MainActivity, AboutActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
    }
}