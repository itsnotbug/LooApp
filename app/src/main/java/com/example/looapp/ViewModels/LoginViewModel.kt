package com.example.looapp.ViewModels

import androidx.lifecycle.ViewModel
import com.example.looapp.AuthenticationRepository

class LoginViewModel:ViewModel() {

    //initialize authentication repo
    private val repository = AuthenticationRepository()

    //function login connects to repository
    fun login(email:String,password:String,callback: (Boolean)->Unit){
        repository.login(email,password,callback)
    }
    override fun onCleared() {
        super.onCleared()
    }
}