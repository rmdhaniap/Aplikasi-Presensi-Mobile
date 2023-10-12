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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.aplikasipresensi.MainActivity
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.api.ApiService
import com.example.aplikasipresensi.data.preference.UserModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val USERNAME_KEY = stringPreferencesKey("username")
private val TOKEN_KEY = stringPreferencesKey("token")

//private val dataStore by lazy {
//    createDataStore(name = "user_data")
//}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_model")
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var btnLogin: Button
    lateinit var mApiClient: ApiService
    lateinit var loading: ProgressDialog
    lateinit var context : Context

    suspend fun saveLoginData(username: String, token: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[TOKEN_KEY] = token
        }
    }

//    private suspend fun checkPreviousLogin() {
//        val preferences = dataStore.data.first()
//        val savedUsername = preferences[USERNAME_KEY]
//        val savedToken = preferences[TOKEN_KEY]
//
//        if (!savedUsername.isNullOrBlank() && !savedToken.isNullOrBlank()) {
//            val intent = Intent(context, MainActivity::class.java)
//            intent.putExtra("name", savedUsername)
//            intent.putExtra("token", savedToken)
//            finish()
//            startActivity(intent)
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        context = this@LoginActivity
        val apiConfig = ApiConfig()
        mApiClient = ApiConfig().getApiService()

        username = findViewById(R.id.inputNama)
        password = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener(this)

//        lifecycleScope.launch {
//            checkPreviousLogin()
//        }
    }

    private suspend fun saveUserToDataStore(userModel: UserModel) {
        val dataStoreKey = stringPreferencesKey("user_model")
        dataStore.edit { preferences ->
            preferences[dataStoreKey] = Gson().toJson(userModel)
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
