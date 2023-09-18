package com.example.looapp.Fragments

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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import java.time.LocalDate

class ContributeFragment : Fragment() {
    private lateinit var binding:FragmentContributeBinding
    private var firebaseConnection = FirebaseConnection()
    private lateinit var placeAutocomplete: PlaceAutocomplete
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContributeBinding.inflate(layoutInflater, container, false)
        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token))
        val passLongitude = requireArguments().getDouble("longitude")
        val passLatitude = requireArguments().getDouble("latitude")
        val passMarkerId = requireArguments().getString("markerId")
        val point = Point.fromLngLat(passLongitude,passLatitude)




//        // Load the map asynchronously
//        mapView.getMapAsync { mapboxMap ->
//            // Set the camera position to the desired latitude and longitude
//            val cameraPosition = CameraPosition.Builder()
//                .target(LatLng(passLatitude, passLongitude))
//                .zoom(15.0F) // Adjust the zoom level as needed
//                .build()
//
//            // Animate the camera to the desired position
//
//            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(cameraPosition))
//       }
        if(passLatitude!=null && passLatitude!=null && passMarkerId!=null && point!=null){
            if (passMarkerId != null) { getPlaceByCoordinates(passMarkerId, point) { restMapbox ->
                    for (restroom in restMapbox) {
                        if (restroom.id == passMarkerId) {
                            binding.etStreet.setText(restroom.street)
                            binding.etCity.setText(restroom.formattedAddress)
                            binding.etCountry.setText(restroom.country)
                            Toast.makeText(context, "${restroom.street}", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                }
            }
        }
        else{
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
                var hasChangingTable =false

                binding.btnGrpAccess.setOnCheckedChangeListener { _, checkedId ->
                    when(checkedId){
                        R.id.rbYes->{isAccessible=true}
                        R.id.rbNo->{isAccessible=false}
                    }
                }
                binding.rbGrpGender.setOnCheckedChangeListener{_, checkedId->
                    when(checkedId){
                        R.id.rbGenderYes->{isUniSex=true}
                        R.id.rbGenderNo->{isUniSex=false}
                    }
                }
                binding.bGChangingTable.setOnCheckedChangeListener{_,checkedId->
                    when(checkedId){
                        R.id.rbChangeYes->{hasChangingTable=true}
                        R.id.rbChangeNo->{hasChangingTable=false}
                    }
                }

                // Compute the values for currentDate, timestampId, and uniqueId inside submitRequested
                val currentDate = LocalDate.now().toString()


                var counter = 100
                fun generateUniqueInt(): Int {
                    return counter++
                }
                val editId = generateUniqueInt()

                submitRequested(nameOfPlace, street, state, city, country, comments, directions, isAccessible, isUniSex, currentDate, editId, passMarkerId,hasChangingTable,passLatitude,passLongitude)
            }
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
            var hasChangingTable =false

            binding.btnGrpAccess.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.rbYes->{isAccessible=true}
                    R.id.rbNo->{isAccessible=false}
                }
            }
            binding.rbGrpGender.setOnCheckedChangeListener{_, checkedId->
                when(checkedId){
                    R.id.rbGenderYes->{isUniSex=true}
                    R.id.rbGenderNo->{isUniSex=false}
                }
            }
            binding.bGChangingTable.setOnCheckedChangeListener{_,checkedId->
                when(checkedId){
                    R.id.rbChangeYes->{hasChangingTable=true}
                    R.id.rbChangeNo->{hasChangingTable=false}
                }
            }

            // Compute the values for currentDate, timestampId, and uniqueId inside submitRequested
            val currentDate = LocalDate.now().toString()


            var counter = 100
            fun generateUniqueInt(): Int {
                return counter++
            }
            val editId = generateUniqueInt()

            submitRequested(nameOfPlace, street, state, city, country, comments, directions, isAccessible, isUniSex, currentDate, editId, passMarkerId,hasChangingTable,passLatitude,passLongitude)
        }
        return binding.root
    }

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
        passMarkerId: String?,
        hasChangingTable: Boolean,
        passLatitude: Double,
        passLongitude: Double
    ) {

        var counter = 100
        fun generateUniqueInt(): Int {
            return counter++
        }
        val restroomItem = passMarkerId?.let {
            RestroomItem(
                it,
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
        }
        if (passMarkerId != null) {
            restroomItem?.let { firebaseConnection.saveRestroomItem(it,passMarkerId) }
        }
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

                            // Check if you have collected all the data and then call the callback
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



