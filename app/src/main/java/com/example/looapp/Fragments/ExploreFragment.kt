package com.example.looapp.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.looapp.LocationPermissionHelper
import com.example.looapp.Model.Restroom
import com.example.looapp.R
import com.example.looapp.Restroom1
import com.example.looapp.RestroomInterface
import com.example.looapp.RestroomItem
import com.example.looapp.Screens.RestroomRatings
import com.example.looapp.databinding.ActivityRatingsDialogBinding
import com.example.looapp.databinding.FragmentExploreBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.gestures.MoveGestureDetector
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
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchResultsView
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList


class ExploreFragment : Fragment(), OnMapClickListener, OnMapLongClickListener {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private val apiEndpoint = "https://zylalabs.com/api/2086/available+public+bathrooms+api/1869/get+public+bathrooms/"
    private val apiKey = "2137|lQ4sNFb5Vb4vWSJyqPXciewVRkW2NIBd7yxDRsxL"
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var binding: FragmentExploreBinding
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
    private var collectionName = "restroom"
    private lateinit var restroom1:Restroom1


    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        private fun onCameraTrackingDismissed() {

        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }
    }

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
//                getRestroom(14.5794,121.035,1,50)
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
                fun displayViewAnnotation(featureId: String) {
                    // Toggle visibility of the annotation if it exists
                    viewAnnotationManager.getViewAnnotationByFeatureId(featureId)
                        ?.toggleViewVisibility()

                }
//                getAllData(collectionName) { result ->
//                    var matchFound = false
//
//                    // Iterate through your data to find the matching restroom
//                    for (restroom in result) {
//                        val restroomMarkerId = restroom.markerId.trim()
//                        if (restroomMarkerId == feature.id()) {
////                            Toast.makeText(
////                                context,
////                                "FID: ${feature.id()}, RID:$restroomMarkerId",
////                                Toast.LENGTH_SHORT
////                            ).show()
//                            val newMarkerId = restroom.markerId
//                            val coordinate = Point.fromLngLat(restroom.longitude, restroom.latitude)
//                            val existingViewAnnotation =
//                                viewAnnotationManager.getViewAnnotationByFeatureId(restroomMarkerId)
//                            if (existingViewAnnotation != null) {
//                                displayViewAnnotation(feature.id().toString())
//                            } else {
//                                addViewAnnotation(coordinate, newMarkerId)
//                            }
//                            matchFound = true
//                            break // Exit the loop since a match is found
//                        }
//                    }
//                    if (!matchFound) {
//                        Toast.makeText(
//                            context,
//                            "Feature ID: ${feature.id()}, No ratings yet",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
                getAllRestroom(collectionName) { result ->
                    var matchFound = false

                    // Iterate through your data to find the matching restroom
                    for (restroom in result) {
                        val restroomMarkerId = restroom.markerId.trim()
                        if (restroomMarkerId == feature.id()) {
                            Toast.makeText(
                                context,
                                "FID: ${feature.id()}, RID:$restroomMarkerId",
                                Toast.LENGTH_SHORT
                            ).show()
                            val newMarkerId = restroom.markerId
                            val coordinate = Point.fromLngLat(restroom.longitude, restroom.latitude)
                            val existingViewAnnotation =
                                viewAnnotationManager.getViewAnnotationByFeatureId(restroomMarkerId)
                            if (existingViewAnnotation != null) {
                                displayViewAnnotation(feature.id().toString())
                            } else {
                                addViewAnnotation(coordinate, newMarkerId)
                            }
                            matchFound = true
                            break // Exit the loop since a match is found
                        }
                    }
                    if (!matchFound) {
                        Toast.makeText(
                            context,
                            "Feature ID: ${feature.id()}, No ratings yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return true
    }

    private fun rateRestroom(point: Point, markerId: String) {
        var myIntent = Intent(context, RestroomRatings::class.java)
        val mBundle = Bundle()
        mBundle.putDouble("latitude", point.latitude())
        mBundle.putDouble("longitude", point.longitude())
        mBundle.putString("markerId", markerId)
        myIntent.putExtras(mBundle)
        startActivity(myIntent)
    }

    private fun handleMarkerPoint(point: Point): Point {
        return point
    }

    private fun handleMarkerClick(point: Point): Boolean {
        return point != null
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
        val currentId =UUID.randomUUID().toString()
        //Retrieve coordinates from the api request
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
        getAllRestroom("restroom1"){restroomItems ->
            for(items in restroomItems){
                val point = Point.fromLngLat(items.longitude,items.latitude)
                pointList.add(Feature.fromGeometry(point, null, items.markerId))
            }
//        }
//        getAllData("restroom") { locationList ->
//            Toast.makeText(context, "${locationList.size} locations loaded.", Toast.LENGTH_SHORT).show()
//            for(location in locationList){
//                val point = Point.fromLngLat(location.longitude,location.latitude)
//                pointList.add(Feature.fromGeometry(point, null, location.markerId))
//            }
            val featureCollection = FeatureCollection.fromFeatures(pointList)
            map.getStyle { style ->
                style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
            }
        }
    }
    private fun documentToRestroomItem(document: DocumentSnapshot): RestroomItem {
        val data = document.data
        if (data != null) {
            return RestroomItem(
                data["markerId"].toString(),
                data["accessible"] as? Boolean ?: false,
                data["approved"] as? Boolean ?: false,
                data["bearing"].toString(),
                data["changing_table"] as? Boolean ?: false,
                data["city"].toString(),
                data["comment"].toString(),
                data["country"].toString(),
                data["created_at"].toString(),
                data["directions"].toString(),
                (data["distance"] as? Number)?.toDouble() ?: 0.0,
                (data["downvote"] as? Number)?.toInt() ?: 0,
                (data["edit_id"] as? Number)?.toInt() ?: 0,
                (data["id"] as? Number)?.toInt() ?: 0,
                (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                data["name"].toString(),
                data["state"].toString(),
                data["street"].toString(),
                data["unisex"] as? Boolean ?: false,
                data["updated_at"].toString(),
                (data["upvote"] as? Number)?.toInt() ?: 0
            )
        }
        return RestroomItem("", false, false, "", false, "", "", "", "", "", 0.0, 0, 0, 0, 0.0, 0.0, "", "", "", false, "", 0)
    }
    private fun getAllRestroom(collectionName: String, callback: (MutableList<RestroomItem>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(collectionName)

        collectionRef.get()
            .addOnSuccessListener { result ->
                val restroomList1 = mutableListOf<RestroomItem>()
                for (document in result) {
                    val restroomItem = documentToRestroomItem(document)
                    restroomList1.add(restroomItem)
                }
                callback(restroomList1)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.d("Firestore", "Error getting documents: $exception")
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
//                viewAnnotation.findViewById<TextView>(R.id.textNativeView).text =
//                   "Tap for more info"
                viewAnnotation.findViewById<ImageView>(R.id.closeNativeView).setOnClickListener { _ ->
                    viewAnnotationManager.removeViewAnnotation(viewAnnotation)
            }
                viewAnnotation.findViewById<Button>(R.id.selectButton).setOnClickListener { b ->
                    val button = b as Button
                    val isSelected = button.text.toString().equals("RATE ME", true)
                    val pxDelta = (if (isSelected) SELECTED_ADD_COEF_DP.dpToPx() else -SELECTED_ADD_COEF_DP.dpToPx()).toInt()
                    button.text = if (isSelected) "BACK" else "RATE ME"
                    if(isSelected){
                        rateRestroom(point,markerId)
                    }
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
    private fun saveRestroomItem(restroomItem: RestroomItem, markerId: String) {
        // Reference to the Firestore collection
        val collectionRef = firestore.collection("restroom1")
        restroomItem.markerId = markerId
        // Add a new document with a randomly generated ID
        collectionRef
            .add(restroomItem)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                // Handle success here
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
                // Handle the error here
            }
    }
    private fun getRestroom(latitude: Double, longitude: Double, page: Int, perPage: Int) {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $apiKey") // Add the bearer token header
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
        val api = Retrofit.Builder()
            .baseUrl(apiEndpoint)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestroomInterface::class.java)
        api.getPublicBathrooms(latitude, longitude, page, perPage)
            .enqueue(object : Callback<Restroom1> {
                override fun onResponse(call: Call<Restroom1>, response: Response<Restroom1>) {
                    if (response.isSuccessful) {
                        restroom1= response.body()!!
                        if (restroom1 != null) {

                            for (r in restroom1) {
                                val restCoordinate = Point.fromLngLat(r.longitude,r.latitude)
                                val id = addMarkerAndReturnId(restCoordinate)
                                saveRestroomItem(r,id)
//                                Toast.makeText(context, "$id", Toast.LENGTH_SHORT).show()

                            }
                        } else {

                            Toast.makeText(context, "NOT FOUND", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Response ${response.code()}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Restroom1>, t: Throwable) {
                    Log.d("MYAPI", "Error $t")
                }

            })
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