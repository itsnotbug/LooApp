package com.example.looapp.Screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.looapp.Model.SplashScreenModel
import com.example.looapp.R
import com.example.looapp.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        binding.lifecycleOwner = this

        //lottie animation toilet paper
        binding.toiletPaper.playAnimation()
        //Coroutine for the splashScreens
        CoroutineScope(Dispatchers.Main).launch {
            delay(SplashScreenModel.SPLASH_DELAY)
            var logIntent = Intent(this@SplashScreen, Login::class.java)
            startActivity(logIntent)
            finish()
        }
    }

}