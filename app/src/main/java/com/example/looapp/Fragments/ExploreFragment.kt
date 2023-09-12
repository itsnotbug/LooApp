package com.example.looapp.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.looapp.LocationPermissionHelper
import com.example.looapp.Model.Restroom
import com.example.looapp.R
import com.example.looapp.databinding.ActivityRatingsDialogBinding
import com.example.looapp.databinding.FragmentExploreBinding
import com.example.looapp.lastKnownLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.QueriedFeature
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchResultsView
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList


class ExploreFragment : Fragment(), OnMapClickListener, OnMapLongClickListener {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var binding: FragmentExploreBinding
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private val pointList = CopyOnWriteArrayList<Feature>()
    private var markerId = 0
    private var markerWidth = 0
    private var markerHeight = 0
    private val asyncInflater by lazy { context?.let { AsyncLayoutInflater(it) } }
    private lateinit var placeAutocomplete: PlaceAutocomplete
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var placeAutocompleteUiAdapter: PlaceAutocompleteUiAdapter
    private lateinit var queryEditText: EditText
    private var collectionName ="restroom"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using DataBindingUtil
        binding = FragmentExploreBinding.inflate(layoutInflater, container, false)
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore, container, false)
        firestore = FirebaseFirestore.getInstance()
        mapView = binding.mapView

        //autocomplete
        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token))
        queryEditText = binding.queryText

        //View Annotation Manager
        viewAnnotationManager = binding.mapView.viewAnnotationManager
        locationPermissionHelper = LocationPermissionHelper(WeakReference(context as Activity?))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        //search
        searchResultsView = binding.searchResultsView
        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration()
            )
        )

        //use of autocomplete
        placeAutocompleteUiAdapter = PlaceAutocompleteUiAdapter(
            view = searchResultsView,
            placeAutocomplete = placeAutocomplete
        )
        context?.let {
            LocationEngineProvider.getBestLocationEngine(it).lastKnownLocation(it) { point ->
                point?.let {
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(9.0)
                            .build()
                    )
                }
            }
        }

        placeAutocompleteUiAdapter.addSearchListener(object :
            PlaceAutocompleteUiAdapter.SearchListener {
            override fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>) {
                // Nothing to do
            }

            override fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
//                openPlaceCard(suggestion)
            }

            override fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion) {
                queryEditText.setText(suggestion.name)
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        markerWidth = bitmap.width
        markerHeight = bitmap.height
        map = binding.mapView.getMapboxMap().apply {
            loadStyle(
                styleExtension = prepareStyle(Style.MAPBOX_STREETS, bitmap)
            ) {
                mapView.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }
                addOnMapClickListener(this@ExploreFragment)
                addOnMapLongClickListener(this@ExploreFragment)
                Toast.makeText(context, STARTUP_TEXT, Toast.LENGTH_LONG).show()
                displayMarkers()
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun getPlaceByCoordinates(id: String, point: Point) {
        //lifecycle  response for autocomplete
        lifecycleScope.launchWhenCreated {
            val response = placeAutocomplete.suggestions(point)
            if (response.isValue) {
                val suggestions = requireNotNull(response.value)
                if (suggestions.isNotEmpty()) {
                    val selectedSuggestion = suggestions.first()
                    val selectionResponse = placeAutocomplete.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        val resultAddress = result.address
                        Toast.makeText(context, "${result.address}", Toast.LENGTH_LONG).show()
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
                            //add in firebase using toilet data class
                            val newToiletLocation = Restroom(
                                id, result.coordinate.longitude(),
                                result.coordinate.latitude(), houseNumber, street, neighborhood,
                                locality, postcode, place, district, region, country,
                                formattedAddress, countryIso1, countryIso2
                            )
                            addData(newToiletLocation)
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


    private fun prepareStyle(styleUri: String, bitmap: Bitmap) = style(styleUri) {
        +image(TOILET_ICON_ID) {
            bitmap(bitmap)
        }
        +geoJsonSource(SOURCE_ID) {
            featureCollection(FeatureCollection.fromFeatures(pointList))
        }
        if (styleUri == Style.SATELLITE_STREETS) {
            +rasterDemSource(TERRAIN_SOURCE) {
                url(TERRAIN_URL_TILE_RESOURCE)
            }
            +terrain(TERRAIN_SOURCE)
        }
        +symbolLayer(LAYER_ID, SOURCE_ID) {
            iconImage(TOILET_ICON_ID)
            iconAnchor(IconAnchor.BOTTOM)
            iconAllowOverlap(false)
        }
    }

    private fun addData(restroom: Restroom) {
        Firebase.firestore.collection("restroom")
            .add(restroom).addOnSuccessListener {
                Log.d("SUCCESS_TAG", "Success!")
            }
            .addOnFailureListener { e ->
                Log.e("SUCCESS_TAG", "Failed! $e")
            }
    }

    private fun getAllData(collectionName: String, callback: (MutableList<Restroom>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(collectionName)
        collectionRef.get()
            .addOnSuccessListener { result ->
                val locationList = mutableListOf<Restroom>()
                for (document in result) {
                    val restroomLocation = Restroom(
                        document.data["markerId"].toString(),
                        document.data["longitude"].toString().toDouble(),
                        document.data["latitude"].toString().toDouble(),
                        document.data["houseNumber"].toString(),
                        document.data["street"].toString(),
                        document.data["neighborhood"].toString(),
                        document.data["locality"].toString(),
                        document.data["postcode"].toString(),
                        document.data["place"].toString(),
                        document.data["district"].toString(),
                        document.data["region"].toString(),
                        document.data["country"].toString(),
                        document.data["formattedAddress"].toString(),
                        document.data["countryIso1"].toString(),
                        document.data["countryIso2"].toString()
                    )

                    locationList.add(restroomLocation)
                }
                callback(locationList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error Occurred!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddLocationDialog() {
        val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle("Add Toilet Location")
        alertDialogBuilder?.setMessage("Would you like to add this location?")
        alertDialogBuilder?.setPositiveButton("Continue") { dialog, _ ->
            dialog.dismiss()

        }
        alertDialogBuilder!!.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        //stupid dont forget this will create the diaglog hehe
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()


    }

    private fun showRatingsPerToilet() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        // Inflate the dialog layout using Data Binding
        val dialogBinding =
            ActivityRatingsDialogBinding.inflate(LayoutInflater.from(requireContext()))

        // Set the root view of the dialog to the root of the inflated layout
        alertDialogBuilder.setView(dialogBinding.root)

        alertDialogBuilder.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onMapLongClick(point: Point): Boolean {
        val markerId = addMarkerAndReturnId(point)

        return true
    }
    override fun onMapClick(point: Point): Boolean {

        map.queryRenderedFeatures(
            RenderedQueryGeometry(map.pixelForCoordinate(point)),
            RenderedQueryOptions(listOf(LAYER_ID), null)
        ) { expected ->
            onFeatureClicked(expected) { feature ->
                fun displayViewAnnotation(featureId:String){
                        // Toggle visibility of the annotation if it exists
                        viewAnnotationManager.getViewAnnotationByFeatureId(featureId)
                            ?.toggleViewVisibility()

                }
                getAllData(collectionName){ result->
                    var matchFound = false

                    // Iterate through your data to find the matching restroom
                    for (restroom in result) {
                        val restroomMarkerId = restroom.markerId.trim()
                        if (restroomMarkerId == feature.id()) {
                            Toast.makeText(context, "FID: ${feature.id()}, RID:$restroomMarkerId", Toast.LENGTH_SHORT).show()
                            val newMarkerId = restroom.markerId
                            val coordinate = Point.fromLngLat(restroom.longitude, restroom.latitude)
                            val existingViewAnnotation = viewAnnotationManager.getViewAnnotationByFeatureId(restroomMarkerId)
                            if(existingViewAnnotation!=null){
                                displayViewAnnotation(feature.id().toString())
                            }
                            else{
                                addViewAnnotation(coordinate, newMarkerId)
                            }
                            matchFound = true
                            break // Exit the loop since a match is found
                        }
                    }
                    if (!matchFound) {
                        Toast.makeText(context, "Feature ID: ${feature.id()}, No matching restroom found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return true
    }
    private fun handleMarkerPoint(point: Point): Point {
      return point
    }

    private fun handleMarkerClick(point: Point): Boolean {
        return point!=null
    }

    private fun onFeatureClicked(
        expected: Expected<String, List<QueriedFeature>>,
        onFeatureClicked: (Feature) -> Unit
    ) {
        if (expected.isValue && expected.value?.isNotEmpty() == true) {
            expected.value?.forEach { queriedFeature ->
                queriedFeature.feature?.let { feature ->
                    onFeatureClicked.invoke(feature)
                }
            }
        }
    }


    private fun View.toggleViewVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    //add marker
    private fun addMarkerAndReturnId(point: Point): String {
        val currentId =  UUID.randomUUID().toString()
        //Retrieve
        getPlaceByCoordinates(currentId,point)
        pointList.add(Feature.fromGeometry(point, null, currentId))
        val featureCollection = FeatureCollection.fromFeatures(pointList)
        map.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
        }
        return currentId
    }


    //display all markers in map
    private fun displayMarkers(){
        getAllData("restroom") { locationList ->
            Toast.makeText(context, "${locationList.size} locations loaded.", Toast.LENGTH_SHORT).show()
            for(location in locationList){
                val point = Point.fromLngLat(location.longitude,location.latitude)
                pointList.add(Feature.fromGeometry(point, null, location.markerId))
            }
            val featureCollection = FeatureCollection.fromFeatures(pointList)
            map.getStyle { style ->
                style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun addViewAnnotation(point: Point, markerId: String) {
        asyncInflater?.let {
            viewAnnotationManager.addViewAnnotation(
                resId = R.layout.minimal_layout,
                options = viewAnnotationOptions {
                    geometry(point)
                    associatedFeatureId(markerId)
                    anchor(ViewAnnotationAnchor.BOTTOM)
                    allowOverlap(false)
                },
                asyncInflater = it
            ) { viewAnnotation ->
                viewAnnotation.visibility = View.GONE
                // calculate offsetY manually taking into account icon height only because of bottom anchoring
                viewAnnotationManager.updateViewAnnotation(
                    viewAnnotation,
                    viewAnnotationOptions {
                        offsetY(markerHeight)
                    }
                )
                viewAnnotation.findViewById<TextView>(R.id.textNativeView).text =
                    "lat=%.2f\nlon=%.2f".format(point.latitude(), point.longitude())
                viewAnnotation.findViewById<ImageView>(R.id.closeNativeView).setOnClickListener { _ ->
                    viewAnnotationManager.removeViewAnnotation(viewAnnotation)
            }
                viewAnnotation.findViewById<Button>(R.id.selectButton).setOnClickListener { b ->
                    val button = b as Button
                    val isSelected = button.text.toString().equals("SELECT", true)
                    val pxDelta = (if (isSelected) SELECTED_ADD_COEF_DP.dpToPx() else -SELECTED_ADD_COEF_DP.dpToPx()).toInt()
                    button.text = if (isSelected) "DESELECT" else "SELECT"
                    viewAnnotationManager.updateViewAnnotation(
                        viewAnnotation,
                        viewAnnotationOptions {
                            selected(isSelected)
                        }
                    )
                    (button.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        bottomMargin += pxDelta
                        rightMargin += pxDelta
                        leftMargin += pxDelta
                    }
                    button.requestLayout()
                }
            }
        }
    }

    private fun Float.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        this@ExploreFragment.resources.displayMetrics
    )
    private companion object {
        const val TOILET_ICON_ID = "blue"
        const val SOURCE_ID = "source_id"
        const val LAYER_ID = "layer_id"
        const val TERRAIN_SOURCE = "TERRAIN_SOURCE"
        const val TERRAIN_URL_TILE_RESOURCE = "mapbox://mapbox.mapbox-terrain-dem-v1"
        const val MARKER_ID_PREFIX = "view_annotation_"
        const val SELECTED_ADD_COEF_DP: Float = 8f
        const val STARTUP_TEXT = "Long click on a map to add a marker and click on a marker to pop-up annotation."
    }
}