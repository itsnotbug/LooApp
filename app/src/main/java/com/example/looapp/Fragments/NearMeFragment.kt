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
import com.google.firebase.firestore.FirebaseFirestore
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
         adapter = ToiletAdapter(mutableListOf())
        recycleView.adapter =adapter
        getAllData("toilets"){coordinates->
            //5) Combine with Adapter
            adapter.updateData(coordinates)
        }
        return binding.root
    }
    private fun getAllData(collectionName: String, callback: (MutableList<Toilet>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(collectionName)
        collectionRef.get()
            .addOnSuccessListener { result ->
                val locationList = mutableListOf<Toilet>()
                for (document in result) {
                    val toiletLocation = Toilet(
                        document.data["markerId"].toString(),
                        document.data["longitude"].toString().toDouble(),
                        document.data["latitude"].toString().toDouble(),)
                    locationList.add(toiletLocation)
                }
                callback(locationList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error Occurred!", Toast.LENGTH_SHORT).show()
            }
    }

}
