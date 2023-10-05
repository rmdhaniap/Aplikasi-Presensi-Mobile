package com.example.aplikasipresensi.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasipresensi.MainActivity
import com.example.aplikasipresensi.data.api.ApiService
import com.example.aplikasipresensi.data.preference.UserPreference
import com.example.aplikasipresensi.databinding.ActivityLoginBinding
import com.example.aplikasipresensi.ui.model.ViewModelFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModel()

        loginViewModel.loginResult.observe(this) {
            if (it.status == true) {
                Toast.makeText(applicationContext, "Berhasil", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(applicationContext, "Gagal", Toast.LENGTH_SHORT).show()
            }
        }

        binding.apply {
            btnLogin.setOnClickListener {
                loginViewModel.login(username = inputNama.text.toString(), password = inputPassword.text.toString())
//                val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                intent.putExtra("name", binding.inputNama.text.toString())
//                startActivity(intent)
            }
        }
    }

    private fun setUpViewModel() {
        loginViewModel = ViewModelProvider(
            this, ViewModelFactory(UserPreference.getInstance(this.dataStore))
        )[LoginViewModel::class.java]

        loginViewModel.loginResult.observe(this) {
            if (it.status == true) {
                Toast.makeText(this@LoginActivity, "Login Sukses ${it.user?.name}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
