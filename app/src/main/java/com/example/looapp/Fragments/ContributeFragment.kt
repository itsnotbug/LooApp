package com.example.looapp.Fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.looapp.FirebaseConnection
import com.example.looapp.Model.RestroomItem
import com.example.looapp.Model.RestroomMapbox
import com.example.looapp.R
import com.example.looapp.databinding.FragmentContributeBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import java.time.LocalDate
import java.util.UUID

class ContributeFragment : Fragment() {
    private lateinit var binding:FragmentContributeBinding
    private var firebaseConnection = FirebaseConnection()
    private lateinit var placeAutocomplete: PlaceAutocomplete
    private var markerId =""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContributeBinding.inflate(layoutInflater, container, false)
        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token))
        val bundle = arguments
        if (bundle != null) {
            val passLongitude = bundle.getDouble("longitude")
            val passLatitude = bundle.getDouble("latitude")
            val passMarkerId = bundle.getString("markerId")
            markerId = passMarkerId.toString()
            val point = Point.fromLngLat(passLongitude, passLatitude)

            // Use the data passed via bundle
            if (point != null && passMarkerId != null) {
                if (passMarkerId != null) {
                    getPlaceByCoordinates(markerId, point) { restMapbox ->
                        for (restroom in restMapbox) {
                            if (restroom.id == passMarkerId) {
                                if(restroom.neighborhood=="null"){
                                    binding.etName.setText("None")
                                    Toast.makeText(context,"${restroom.neighborhood}",Toast.LENGTH_SHORT).show()
                                }else{
                                    binding.etName.setText(restroom.neighborhood)
                                }
                                if(restroom.street=="null"){
                                    binding.etStreet.setText("None")
                                }else{
                                    binding.etStreet.setText(restroom.street)
                                }
                                if(restroom.formattedAddress=="null"){
                                    binding.etCity.setText("None")
                                }else{
                                    binding.etCity.setText(restroom.formattedAddress)
                                }
                                binding.etCountry.setText(restroom.country)
//                                Toast.makeText(context, "${restroom.street}", Toast.LENGTH_SHORT)
//                                    .show()
                                break
                            }
                        }
                    }
                }
            }else{
                  markerId = UUID.randomUUID().toString()
            }

            binding.btnSubmitRequest.setOnClickListener {
                val nameOfPlace = binding.etName.text.toString()
                val street = binding.etStreet.text.toString()
                val state = binding.etState.text.toString()
                val city = binding.etCity.text.toString()
                val country = binding.etCountry.text.toString()
                val comments = binding.etComments.text.toString()
                val directions = binding.etDirections.text.toString()
                var isAccessible = false
                var isUniSex = false
                var hasChangingTable = false

                binding.btnGrpAccess.setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.rbYes -> {
                            isAccessible = true
                        }

                        R.id.rbNo -> {
                            isAccessible = false
                        }
                    }
                }
                binding.rbGrpGender.setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.rbGenderYes -> {
                            isUniSex = true
                        }

                        R.id.rbGenderNo -> {
                            isUniSex = false
                        }
                    }
                }
                binding.bGChangingTable.setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.rbChangeYes -> {
                            hasChangingTable = true
                        }

                        R.id.rbChangeNo -> {
                            hasChangingTable = false
                        }
                    }
                }

                // Compute the values for currentDate, timestampId, and uniqueId inside submitRequested
                val currentDate = LocalDate.now().toString()
                var counter = 100
                fun generateUniqueInt(): Int {
                    return counter++
                }

                val editId = generateUniqueInt()

                val result = submitRequested(
                    nameOfPlace,
                    street,
                    state,
                    city,
                    country,
                    comments,
                    directions,
                    isAccessible,
                    isUniSex,
                    currentDate,
                    editId,
                    markerId,
                    hasChangingTable,
                    passLatitude,
                    passLongitude
                )
                if(result){
                    Toast.makeText(context,"Submitted successfully!",Toast.LENGTH_SHORT).show()
                    val exploreFragment = ExploreFragment()
                    val transaction = fragmentManager?.beginTransaction()
                    transaction?.replace(R.id.fragmentContainerView,exploreFragment)
                    transaction?.addToBackStack(null)
                    transaction?.commit()
                }else{
                    Toast.makeText(context,"Submission failed!",Toast.LENGTH_SHORT).show()
                }
            }

        }
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun submitRequested(
        nameOfPlace: String,
        street: String,
        state: String,
        city: String,
        country: String,
        comments: String,
        directions: String,
        isAccessible: Boolean,
        isUniSex: Boolean,
        currentDate: String,
        editId: Int,
        markerId: String,
        hasChangingTable: Boolean,
        passLatitude: Double,
        passLongitude: Double
    ):Boolean {

        var counter = 100
        fun generateUniqueInt(): Int {
            return counter++
        }
        val id = generateUniqueInt()
        val restroomItem =
            RestroomItem(
                markerId,
                isAccessible,
                true,
                "",
                hasChangingTable,
                city,
                comments,
                country,
                currentDate,
                directions,
                0.0,
                0,
                id,
                id,
                passLatitude,
                passLongitude,
                nameOfPlace,
                state,
                street,
                isUniSex,
                "",
                0
            )

            restroomItem?.let { firebaseConnection.saveRestroomItem(it,markerId) }
        return true
    }

    private fun getPlaceByCoordinates(id: String, point: Point, onMapCoordinatesReceived: (List<RestroomMapbox>) -> Unit) {
        // Initialize an empty list to collect data
        val mapToiletList = mutableListOf<RestroomMapbox>()
        // Lifecycle response for autocomplete
        lifecycleScope.launchWhenCreated {
            val response = placeAutocomplete.suggestions(point)
            if (response.isValue) {
                val suggestions = requireNotNull(response.value)
                if (suggestions.isNotEmpty()) {
                    val selectedSuggestion = suggestions.first()
                    Toast.makeText(context, "${suggestions.first()}", Toast.LENGTH_SHORT).show()
                    val selectionResponse = placeAutocomplete.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        val resultAddress = result.address
                        Log.i("address", "${result.address}")
                        val dataMap = parseResultName(resultAddress)
                        if (dataMap.isNotEmpty()) {
                            // Access fields from dataMap
                            val houseNumber = dataMap["houseNumber"]
                            val street = dataMap["street"]
                            val neighborhood = dataMap["neighborhood"]
                            val locality = dataMap["locality"]
                            val postcode = dataMap["postcode"]
                            val place = dataMap["place"]
                            val district = dataMap["district"]
                            val region = dataMap["region"]
                            val country = dataMap["country"]
                            val formattedAddress = dataMap["formattedAddress"]
                            val countryIso1 = dataMap["countryIso1"]
                            val countryIso2 = dataMap["countryIso2"]

                            val newToiletLocation = RestroomMapbox(
                                id, result.name, result.coordinate.longitude(),
                                result.coordinate.latitude(), houseNumber, street, neighborhood,
                                locality, postcode, place, district, region, country,
                                formattedAddress, countryIso1, countryIso2
                            )

                            mapToiletList.add(newToiletLocation)

                            // Check the all data and then call the callback
                            if (mapToiletList.size == suggestions.size) {
                                onMapCoordinatesReceived(mapToiletList)
                            }
                        } else {
                            // Handle the case when the list is empty
                            Toast.makeText(context, "Empty mapping of data", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.onError { e ->
                        Log.i("callApi", "An error occurred during selection", e)
                    }
                }
            } else {
                Log.i("callApiError", "Place Autocomplete error", response.error)
            }
        }
    }

    companion object {
        fun newInstance(
            longitude: Double? = null,
            latitude: Double? = null,
            markerId: String? = null
        ): ContributeFragment {
            val fragment = ContributeFragment()
            val args = Bundle()
            if (longitude != null) {
                args.putDouble("longitude", longitude)
            }
            if (latitude != null) {
                args.putDouble("latitude", latitude)
            }
            if (markerId != null) {
                args.putString("markerId", markerId)
            }
            fragment.arguments = args
            return fragment
        }

        // Create another newInstance method without any arguments
        fun newInstance(): ContributeFragment {
            return ContributeFragment()
        }
    }

    private fun parseResultName(input: PlaceAutocompleteAddress?): Map<String, String> {
        return input?.toString()
            ?.removePrefix("PlaceAutocompleteAddress(")
            ?.removeSuffix(")")
            ?.split(", ")
            ?.mapNotNull {
                val keyValue = it.split("=")
                if (keyValue.size == 2) {
                    keyValue[0] to keyValue[1]
                } else {
                    null // Skip invalid key-value pairs
                }
            }
            ?.toMap()
            ?: emptyMap()
    }
}



