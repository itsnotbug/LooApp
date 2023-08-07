package com.example.looapp

import com.google.firebase.auth.FirebaseAuth

class AuthenticationRepository {

    //auth from firebase
    private val auth= FirebaseAuth.getInstance()
    //login
    fun login(email:String,password: String,callback: (Boolean) -> Unit){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task->
                callback.invoke(task.isSuccessful)
            }
    }

    //register
    fun register(email: String,password: String,callback: (Boolean) -> Unit){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task->
                callback.invoke(task.isSuccessful)
            }
    }

    //logout
    fun logout(){
    }
}
