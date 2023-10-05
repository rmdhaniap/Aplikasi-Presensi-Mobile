package com.example.aplikasipresensi.ui.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasipresensi.MainActivity
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.api.ApiService
import com.example.aplikasipresensi.data.preference.UserPreference
import com.example.aplikasipresensi.databinding.ActivityLoginBinding
import com.example.aplikasipresensi.ui.model.ViewModelFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var btnLogin: Button
    lateinit var mApiClient: ApiService
    lateinit var loading: ProgressDialog
    lateinit var context : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        context = this@LoginActivity
        val apiConfig = ApiConfig();
        mApiClient = ApiConfig().getApiService()

        username = findViewById(R.id.inputNama)
        password = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btnLogin) {
            if (TextUtils.isEmpty(username.text.toString())) {
                username.error = "Username harus diisi"
                if (TextUtils.isEmpty(password.text.toString())) {
                    password.error = "Password harus diisi"
                }
            } else if (TextUtils.isEmpty(password.text.toString())) {
                password.error = "Password harus diisi"
            } else {
                requestLogin(username.text.toString(), password.text.toString())
            }
        }
    }

    fun requestLogin(username: String, password: String) {
        loading = ProgressDialog.show(this, null, "Harap Tunggu...", true, false)
        mApiClient.login(username, password)?.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(response.body().toString())
                        val status = json.getBoolean("status")
                        val message = json.getString("message")

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        if (status) {
                            val userObject = json.getJSONObject("user")
                            val name = userObject.getString("name")

                            val intent = Intent(context, MainActivity::class.java)
                            intent.putExtra("name", name)
                            finish()
                            startActivity(intent)
                        } else {
                            loading.dismiss()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    loading.dismiss()
                } else {
                    loading.dismiss()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.i("debug", "onFailure: ERROR > $t")
                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                loading.dismiss()
            }
        })
    }


//    private lateinit var binding: ActivityLoginBinding
//    private lateinit var loginViewModel: LoginViewModel
//    private lateinit var context : Context
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        setUpViewModel()

//        loginViewModel.loginResult.observe(this) {
//            if (it.status == true) {
//                Toast.makeText(applicationContext, "Berhasil", Toast.LENGTH_SHORT).show()
//            }
//            else {
//                Toast.makeText(applicationContext, "Gagal", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        binding.apply {
//            btnLogin.setOnClickListener {
//                loginViewModel.login(username = inputNama.text.toString(), password = inputPassword.text.toString())
//                //val intent = Intent(, MainActivity::class.java)
////                intent.putExtra("name", binding.inputNama.text.toString())
////                startActivity(intent)
//            }
//        }
//    }

//    private fun setUpViewModel() {
//        loginViewModel = ViewModelProvider(
//            this, ViewModelFactory(UserPreference.getInstance(this.dataStore))
//        )[LoginViewModel::class.java]
//
//        loginViewModel.loginResult.observe(this) {
//            val name = binding.inputNama
//            if (it.status == true) {
//                Toast.makeText(this@LoginActivity, "Login Sukses ${it.user?.name}", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//            else {
//                Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
