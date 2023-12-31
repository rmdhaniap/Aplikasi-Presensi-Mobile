package com.example.aplikasipresensi.ui.login

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aplikasipresensi.MainActivity
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.api.ApiService
import com.example.aplikasipresensi.data.preference.UserModel
import com.example.aplikasipresensi.databinding.ActivityLoginBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val USERNAME_KEY = stringPreferencesKey("username")
private val TOKEN_KEY = stringPreferencesKey("token")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_model")
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var btnLogin: Button
    lateinit var mApiClient: ApiService
    lateinit var loading: ProgressDialog
    lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this@LoginActivity
        val apiConfig = ApiConfig()
        mApiClient = ApiConfig().getApiService()

        username = findViewById(R.id.inputNama)
        password = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener(this)

        binding.btnBackHome.setOnClickListener {
            finish()
        }

        playAnimation()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imgLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 600
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val back = ObjectAnimator.ofFloat(binding.btnBackHome, View.ALPHA, 1f).setDuration(100)
        val textLogin = ObjectAnimator.ofFloat(binding.txtLogin, View.ALPHA, 1f).setDuration(100)
        val name = ObjectAnimator.ofFloat(binding.inputNama, View.ALPHA, 1f).setDuration(100)
        val pass = ObjectAnimator.ofFloat(binding.inputPassword, View.ALPHA, 1f).setDuration(100)
        val btn = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(100)
        val img = ObjectAnimator.ofFloat(binding.imgLogin, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(back, textLogin, img , name, pass, btn)
            start()
        }
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
                        val token = json.getString("token")

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        if (status) {
                            val userObject = json.getJSONObject("user")
                            val name = userObject.getString("name")

                            val intent = Intent(context, MainActivity::class.java)
                            intent.putExtra("name", name)
                            intent.putExtra("token", token)
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
}
