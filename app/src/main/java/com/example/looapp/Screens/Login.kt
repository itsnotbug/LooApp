package com.example.looapp.Screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.looapp.R
import com.example.looapp.viewModel.LoginViewModel
import com.example.looapp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner=this

        //initialized view Model holder
        var viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding.loginViewModel =viewModel

        //observer
        viewModel.loginResult.observe(this, Observer { success->
            if(success){
                Toast.makeText(applicationContext, "Login Successfully.",
                    Toast.LENGTH_SHORT).show()
                    val mainInt = Intent(this, MainActivity::class.java)
                    startActivity(mainInt)
                   finish()
            }else{
                Toast.makeText(applicationContext,"Authentication Failed!",
                    Toast.LENGTH_SHORT).show()
                Log.w("LOG ERROR", "signInWithEmail:failure")
            }
        })
        //Login
        binding.btnLogin.setOnClickListener {
            val email = binding.editTxtEmail.text.toString()
            val password = binding.editTxtPass.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext,"Fields should not be empty",
                    Toast.LENGTH_SHORT).show()
            }else{
               viewModel.performLogin(email,password)
            }
        }
        binding.txtRegister.setOnClickListener{
            val registerIntent =Intent(this@Login, Registration::class.java)
            startActivity(registerIntent)
            finish()
        }
    }
}