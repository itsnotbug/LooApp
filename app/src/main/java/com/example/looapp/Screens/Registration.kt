package com.example.looapp.Screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.looapp.ViewModels.RegistrationViewModel
import com.example.looapp.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.*
import com.google.firebase.ktx.Firebase

class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialized view Model holder
        var viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

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
                    viewModel.register(email,secondPassword) { isSuccess ->
                        if (isSuccess) {
                            Toast.makeText(
                                applicationContext, "Account created!",
                                Toast.LENGTH_SHORT).show()
                            val logIntent = Intent(this, Login::class.java)
                            startActivity(logIntent)
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext, "Password does not match",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Log.w("LOGError", "createUserWithEmail:failure")
                    Toast.makeText(applicationContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
                
            }
        }
    }
}