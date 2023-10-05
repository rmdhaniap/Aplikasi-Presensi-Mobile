package com.example.aplikasipresensi.ui.login

import android.util.Log
import androidx.datastore.preferences.protobuf.Api
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.preference.UserModel
import com.example.aplikasipresensi.data.preference.UserPreference
import com.example.aplikasipresensi.data.response.LoginResponse
import com.example.aplikasipresensi.data.response.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference): ViewModel() {

    val _loginResult = MutableLiveData<UserModel>()
    val loginResult: LiveData<UserModel> = _loginResult

    fun login(username: String, password: String) {
        val client = ApiConfig.getApiService().login(username, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.postValue(
                        response.body()?.let {
                            UserModel(it.message, it.status, it.token, it.user)
                        }
                    )
                    saveToken(response.body()?.token.toString())
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("LoginViewModel", "onFailure: ${t.message}")
            }
        })
    }
    private fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }

    fun saveUser(user: String) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }
}