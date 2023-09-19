package com.example.looapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.looapp.databinding.ActivityToiletDetailsBinding
import com.example.looapp.viewModel.LoginViewModel

class ToiletDetails : AppCompatActivity() {
    private lateinit var binding: ActivityToiletDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_toilet_details)
        binding.lifecycleOwner = this

        binding.etDName.text = intent.getStringExtra("name")
        binding.etDStreet.text = intent.getStringExtra("street")
        binding.etDCity.text = intent.getStringExtra("city")
        binding.etDState.text =intent.getStringExtra("state")
        val country = intent.getStringExtra("country")
        if(country=="PH"){
            binding.etDCountry.text ="Philippines"
        }else{
            binding.etDCountry.text =country
        }

        val accessible = intent.getBooleanExtra("accessible",false)
        if(accessible){
            binding.etDAccessible.text = "Public Restroom"

        }else{
            binding.etDAccessible.text = "Private Restroom"
        }
        val unisex = intent.getBooleanExtra("unisex",false)
        if(unisex){
            binding.etDUnisex.text ="Unisex"
        }else{
            binding.etDUnisex.text ="Not specified"
        }
        binding.etDDirections.text =intent.getStringExtra("directions")
        binding.etDComments.text =intent.getStringExtra("comments")
    }
}