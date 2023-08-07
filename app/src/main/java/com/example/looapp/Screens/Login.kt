package com.example.looapp.Screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.looapp.ViewModels.LoginViewModel
import com.example.looapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialized view Model holder
        var viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        //Login
        binding.btnLogin.setOnClickListener {
            val email = binding.editTxtEmail.text.toString()
            val password = binding.editTxtPass.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext,"Fields should not be empty",
                    Toast.LENGTH_SHORT).show()
            }else{
               viewModel.login(email,password){ isSuccess->
                   if(isSuccess){
                       Toast.makeText(applicationContext, "Login Successfully.",
                           Toast.LENGTH_SHORT).show()
                       val mainInt = Intent(this, MainActivity::class.java)
                       startActivity(mainInt)
                       finish()
                   }else{
                       Log.w("LOG ERROR", "signInWithEmail:failure")
                       Toast.makeText(applicationContext, "Authentication failed.",
                           Toast.LENGTH_SHORT).show()
                   }
               }
            }
        }
        binding.txtRegister.setOnClickListener{
            val registerIntent =Intent(this@Login, Registration::class.java)
            startActivity(registerIntent)
            finish()
        }
    }
}