package com.example.aplikasipresensi.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasipresensi.data.preference.UserPreference
import com.example.aplikasipresensi.ui.login.LoginViewModel

class ViewModelFactory(private val pref: UserPreference): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            else -> throw java.lang.IllegalArgumentException("Unknown ViewModel class: "+ modelClass.name)
        }
    }
}