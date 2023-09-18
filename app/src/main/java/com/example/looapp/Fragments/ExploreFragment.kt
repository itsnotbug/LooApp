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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.looapp.FirebaseConnection
import com.example.looapp.LocationPermissionHelper
import com.example.looapp.Model.Restroom
import com.example.looapp.R
import com.example.looapp.Model.RestroomInterface
import com.example.looapp.Screens.RestroomRatings
import com.example.looapp.databinding.FragmentExploreBinding
import com.google.firebase.firestore.FirebaseFirestore
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
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
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


@Suppress("DEPRECATION")
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
    private lateinit var restroom:Restroom
    private var fbConnect = FirebaseConnection()

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore, container, false)
        mapView = binding.mapView

        //View Annotation Manager
        viewAnnotationManager = binding.mapView.viewAnnotationManager
        locationPermissionHelper = LocationPermissionHelper(WeakReference(context as Activity?))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(15.0)
                .build()
        )
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        markerWidth = bitmap.width
        markerHeight = bitmap.height
        map = binding.mapView.getMapboxMap().apply {
            loadStyle(
                styleExtension = prepareStyle(Style.MAPBOX_STREETS, bitmap)
            ) {
                addOnMapClickListener(this@ExploreFragment)
                addOnMapLongClickListener(this@ExploreFragment)
                Toast.makeText(context, STARTUP_TEXT, Toast.LENGTH_LONG).show()
//                getRestroom(14.5794,121.035,1,20)
                displayMarkers()
            }
        }
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
    private fun showAddLocationDialog(point: Point) {
        val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle("Add Toilet Location")
        alertDialogBuilder?.setMessage("Would you like to add this location?")
        alertDialogBuilder?.setPositiveButton("Continue") { dialog, _ ->
            val bundle = Bundle()
            bundle.putDouble("latitude", point.latitude())
            bundle.putDouble("longitude",point.longitude())
            bundle.putString("markerId",addMarkerAndReturnId(point))
            val contributeFragment = ContributeFragment()
            contributeFragment.arguments = bundle
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainerView,contributeFragment)
            transaction?.addToBackStack(null)

            try {
                transaction?.commit()

            } catch (e: Exception) {
                e.printStackTrace()
            }


            dialog.dismiss()
        }
        alertDialogBuilder!!.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        //stupid dont forget this will create the diaglog hehe
        val alertDialog: AlertDialog = alertDialogBuilder.create()
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

    /*Long touch or click creates marker and generate markerId,
      this function returns true when clicked/touched.
       */
    override fun onMapLongClick(point: Point): Boolean {
        showAddLocationDialog(point)
        return true
    }

    //This function enables the user to tap/click the markers on the map
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
                fbConnect.getAllRestroom(collectionName) { result ->
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
                            "Feature ID: ${feature.id()}, Recently added, wait for approval",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return true
    }

    // Pass the coordinates and marker through intent - bundle
    private fun rateRestroom(point: Point, markerId: String) {
        var myIntent = Intent(context, RestroomRatings::class.java)
        val mBundle = Bundle()
        mBundle.putDouble("latitude", point.latitude())
        mBundle.putDouble("longitude", point.longitude())
        mBundle.putString("markerId", markerId)
        myIntent.putExtras(mBundle)
        startActivity(myIntent)
    }

    //Get the clicks on feature collection
    private fun onFeatureClicked(
        expected: Expected<String, List<QueriedFeature>>,
        onFeatureClicked: (Feature) -> Unit
    ) {
        if (expected.isValue && expected.value?.isNotEmpty() == true) {
            expected.value?.forEach { queriedFeature ->
                queriedFeature.feature.let { feature ->
                    onFeatureClicked.invoke(feature)
                }
            }
        }
    }

    //Display the toggle visibilty of view annotation
    private fun View.toggleViewVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    //Generate unique marker id and add it on feature collection
    private fun addMarkerAndReturnId(point: Point): String {
        val currentId =UUID.randomUUID().toString()
        pointList.add(Feature.fromGeometry(point, null, currentId))
        isExistingOnApi(collectionName, currentId)
        val featureCollection = FeatureCollection.fromFeatures(pointList)
        map.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
        }
        return currentId
    }
    private fun isExistingOnApi(collectionName: String, currentId: String):Boolean {
        val selectedRestroom= getOneRestroom(collectionName,currentId)
        return selectedRestroom!=null
    }
    private fun getOneRestroom(collectionName: String, markerId: String) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(collectionName)
        collectionRef
            .whereEqualTo("markerId", markerId) // Filter by markerId field
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val restroom = document.data
                    Toast.makeText(context, "$restroom", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No matching document found in api", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error Occurred: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    //Display Markers on map
    private fun displayMarkers(){
        fbConnect.getAllRestroom("restroom"){restroomItems ->
            for(items in restroomItems){
                val point = Point.fromLngLat(items.longitude,items.latitude)
                pointList.add(Feature.fromGeometry(point, null, items.markerId))
            }
            val featureCollection = FeatureCollection.fromFeatures(pointList)
            map.getStyle { style ->
                style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
            }
        }
    }
    //Create annotation for markers
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

    //Retrofit for connecting to client and getting the api request
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
            .enqueue(object : Callback<Restroom> {
                override fun onResponse(call: Call<Restroom>, response: Response<Restroom>) {
                    if (response.isSuccessful) {
                        restroom= response.body()!!
                        for (r in restroom) {
                            val restCoordinate = Point.fromLngLat(r.longitude,r.latitude)
                            val id = addMarkerAndReturnId(restCoordinate)
                            fbConnect.saveRestroomItem(r,id)
                        }
                    } else {
                        Toast.makeText(context, "Response ${response.code()}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                override fun onFailure(call: Call<Restroom>, t: Throwable) {
                    Log.d("MYAPI", "Error $t")
                }
            })
    }



    //Change the dimension for view annotation
    private fun Float.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        this@ExploreFragment.resources.displayMetrics
    )

    interface OnLocationAddedListener {
        fun onLocationAdded(point: Point)
    }
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