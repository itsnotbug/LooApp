package com.example.looapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RegistrationViewModel:ViewModel() {
    private val auth= FirebaseAuth.getInstance()
    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult:LiveData<Boolean> get() = _registrationResult


    //perform registration
    fun register(email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task->
               _registrationResult.value = task.isSuccessful
            }
    }
    override fun onCleared() {
        super.onCleared()
    }
}