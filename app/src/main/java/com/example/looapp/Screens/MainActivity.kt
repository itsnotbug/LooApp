package com.example.looapp.Screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.looapp.Fragments.ContributeFragment
import com.example.looapp.Fragments.ExploreFragment
import com.example.looapp.Fragments.NearMeFragment
import com.example.looapp.Fragments.TransactFragment
import com.example.looapp.R
import com.example.looapp.databinding.ActivityMainBinding
import com.example.looapp.viewModel.LoginViewModel
import com.example.looapp.viewModel.MainActivityViewModel


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.lifecycleOwner=this

        //Initialize fragments for bottom navigation
        val exploreFragment =  ExploreFragment()
        val nearMeFragment  =  NearMeFragment()
        val transactFragment = TransactFragment()
        val contributeFragment = ContributeFragment()

        //Fragments
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView,exploreFragment)
            commit()
        }

        //initialized view Model holder
        var viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        binding.mainViewModel =viewModel


        //Bottom navigation
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.itemExplore-> {
                    this.supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainerView, exploreFragment)
                        commit()
                    }
                }
                R.id.itemNearMe -> {
                    this.supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainerView, nearMeFragment)
                        commit()
                    }

                }
                R.id.itemContribute -> {
                    this.supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainerView, contributeFragment)
                        commit()
                    }
                }
                R.id.itemTransact->{
                    this.supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainerView, transactFragment)
                        commit()
                    }
                }
            }
            true
        }
    }
}