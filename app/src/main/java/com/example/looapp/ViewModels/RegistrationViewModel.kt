package com.example.looapp.ViewModels

import androidx.lifecycle.ViewModel
import com.example.looapp.AuthenticationRepository

class RegistrationViewModel:ViewModel() {

    //initialize repository
    private val repository= AuthenticationRepository()

    //register
    fun register(email:String,password:String,callback: (Boolean)->Unit){
        repository.register(email,password,callback)
    }
    override fun onCleared() {
        super.onCleared()
    }
}