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
import com.example.looapp.viewModel.RegistrationViewModel
import com.example.looapp.databinding.ActivityRegistrationBinding
import com.example.looapp.R


class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_registration)
        binding.lifecycleOwner=this

        //Initialized view Model holder
        var viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
        binding.registerViewModel =viewModel

        //observer
        viewModel.registrationResult.observe(this, Observer { success->
            if(success){
                Toast.makeText(
                    applicationContext, "Account created!",
                    Toast.LENGTH_SHORT).show()
                val logIntent = Intent(this, Login::class.java)
                startActivity(logIntent)
                finish()
            }else{
                Log.w("LOGError", "createUserWithEmail:failure")
                Toast.makeText(applicationContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        })

        //Register
        binding.btnRegister.setOnClickListener {
            val email = binding.edtTxtEmailAddress.text.toString()
            val username= binding.edtTxtUserName.text.toString()
            val firstPassword = binding.edtTxtPassword.text.toString()
            val secondPassword = binding.edtTxtPasswordMatch.text.toString()

            if(TextUtils.isEmpty(email) ||TextUtils.isEmpty(username)||
                TextUtils.isEmpty(firstPassword) || TextUtils.isEmpty(secondPassword)){
                Toast.makeText(applicationContext,"Fields are empty, fill up to continue",
                    Toast.LENGTH_SHORT).show()
            }else{
                if(firstPassword==secondPassword){
                    viewModel.register(email,secondPassword)

                }
            }
        }
        //Login
        binding.txtLogin.setOnClickListener {
            var myIntent = Intent(this@Registration,Login::class.java)
            startActivity(myIntent)
            finish()
        }
    }
}