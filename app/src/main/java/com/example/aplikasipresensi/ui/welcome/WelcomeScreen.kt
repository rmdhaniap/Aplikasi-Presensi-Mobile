package com.example.aplikasipresensi.ui.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.databinding.ActivityWelcomeScreenBinding
import com.example.aplikasipresensi.ui.login.LoginActivity

class WelcomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.btnLanjut.setOnClickListener {
            intent = Intent(this@WelcomeScreen, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}