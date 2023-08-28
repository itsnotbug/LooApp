package com.example.looapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Adapters.ToiletAdapter
import com.example.looapp.Model.Toilet
import com.example.looapp.R
import com.example.looapp.databinding.FragmentNearMeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NearMeFragment : Fragment() {
    private lateinit var binding:FragmentNearMeBinding
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: ToiletAdapter
    private var toiletLocation = mutableListOf<Toilet>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNearMeBinding.inflate(layoutInflater,container,false)
        //2.) Set up recycleView Binding
        recycleView = binding.recyclerView
        //3.) Set up layout
        recycleView.layoutManager = LinearLayoutManager(context)
        //4.) Initialize firebase connection
        //declare firebasedb
//        val db = Firebase.firestore
        //5)  Get all Data from Firebase
        getAllData("toilets")
        //5) Combine with Adapter
        adapter = ToiletAdapter(toiletLocation)
        return binding.root
    }
    private fun getAllData(collectionName: String) {
        val db = Firebase.firestore
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result->
                for(document in result){
                    var newToiletLocation = Toilet(document.data["longitude"].toString().toDouble(),document.data["latitude"].toString().toDouble())
                    toiletLocation.add(newToiletLocation)

                }
                recycleView.adapter = ToiletAdapter(toiletLocation)
            }
            .addOnFailureListener { _->
                Toast.makeText(context,"FAILED TO RETRIEVE DATA",Toast.LENGTH_SHORT).show()
            }
    }
}
