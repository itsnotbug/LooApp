package com.example.looapp.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Adapters.RestroomAdapter
import com.example.looapp.FirebaseConnection
import com.example.looapp.Model.RestroomItem
import com.example.looapp.databinding.FragmentNearMeBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class NearMeFragment : Fragment() {
    private lateinit var binding:FragmentNearMeBinding
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: RestroomAdapter
    private lateinit var restroom: MutableList<RestroomItem>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNearMeBinding.inflate(layoutInflater,container,false)
        // Set up recycleView Binding
        recycleView = binding.recyclerView
        // Set up layout
        recycleView.layoutManager = LinearLayoutManager(context)
        // Get all Data from Firebase
        adapter = RestroomAdapter(mutableListOf())
        recycleView.adapter =adapter

        val firebaseConnection = FirebaseConnection()
        firebaseConnection.getAllRestroom("restroom"){ coordinates->
            adapter.updateData(coordinates)
            restroom =coordinates
        }
          binding.searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener,
              androidx.appcompat.widget.SearchView.OnQueryTextListener {
              override fun onQueryTextSubmit(query: String?): Boolean {
                  return false
              }

              override fun onQueryTextChange(newText: String?): Boolean {
                  filterList(newText)
                  return true
              }
          })
        return binding.root
    }

    private fun filterList(query: String?) {
            if(query!=null){
                val filteredList = ArrayList<RestroomItem>()
                for (i in restroom){
                    if(i.street.lowercase(Locale.ROOT)!!.contains(query)){
                        filteredList.add(i)
                    }
                }
                if(filteredList.isEmpty()){
                    Toast.makeText(context,"No data found",Toast.LENGTH_SHORT).show()
                }
                else{
                    adapter.setFiltered(filteredList)
                }
            }
        }
}
