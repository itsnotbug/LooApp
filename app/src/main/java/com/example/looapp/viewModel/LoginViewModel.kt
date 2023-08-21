package com.example.looapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel:ViewModel() {
    private val auth= FirebaseAuth.getInstance()
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult:LiveData<Boolean> get() = _loginResult

    //perform login
    fun performLogin(username:String,password:String) {
        auth.signInWithEmailAndPassword(username,password)
            .addOnCompleteListener { task->
                _loginResult.value = task.isSuccessful
            }
    }

    override fun onCleared() {
        super.onCleared()
    }
}