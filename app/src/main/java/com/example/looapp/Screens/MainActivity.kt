package com.example.looapp.Screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.example.looapp.Fragments.ContributeFragment
import com.example.looapp.Fragments.ExploreFragment
import com.example.looapp.Fragments.NearMeFragment
import com.example.looapp.Fragments.TransactFragment
import com.example.looapp.R
import com.example.looapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize fragments for bottom navigation
        val exploreFragment =  ExploreFragment()
        val nearMeFragment  =  NearMeFragment()
        val transactFragment = TransactFragment()
        val contributeFragment = ContributeFragment()

        //F ragments
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView,exploreFragment)
            commit()
        }

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