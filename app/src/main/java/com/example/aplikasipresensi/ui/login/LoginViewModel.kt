package com.example.aplikasipresensi.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasipresensi.data.preference.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference): ViewModel() {

//    val _loginResult = MutableLiveData<UserModel>()
//    val loginResult: LiveData<UserModel> = _loginResult
//
//    fun login(username: String, password: String) {
//
//        val client = ApiConfig.getApiService().login(username, password)
//        client.enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//                if (response.isSuccessful && response.body() != null) {
//                    _loginResult.postValue(
//                        response.body()?.let {
//                            UserModel(it.message, it.status, it.token, it.user)
//                        }
//                    )
//                    saveToken(response.body()?.token.toString())
//                }
//            }
//
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                Log.d("LoginViewModel", "onFailure: ${t.message}")
//            }
//        })
//    }
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