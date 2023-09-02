package com.example.looapp.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.fragment.app.Fragment
import com.example.looapp.LocationPermissionHelper
import com.example.looapp.Model.Toilet
import com.example.looapp.R
import com.example.looapp.databinding.FragmentExploreBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExploreBinding.inflate(layoutInflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        mapView = binding.mapView

        viewAnnotationManager = binding.mapView.viewAnnotationManager

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
        markerWidth = bitmap.width
        markerHeight = bitmap.height

        map = binding.mapView.getMapboxMap().apply {
            loadStyle(
                styleExtension = prepareStyle(Style.MAPBOX_STREETS, bitmap)
            ) {
                addOnMapClickListener(this@ExploreFragment)
                addOnMapLongClickListener(this@ExploreFragment)
                Toast.makeText(context, STARTUP_TEXT, Toast.LENGTH_LONG).show()
                displayMarkers()
            }

        }



    // Inflate the layout for this fragment
        return binding.root
    }

    private fun prepareStyle(styleUri: String, bitmap: Bitmap) = style(styleUri) {
        +image(RED_ICON_ID) {
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
            iconImage(RED_ICON_ID)
            iconAnchor(IconAnchor.BOTTOM)
            iconAllowOverlap(false)
        }
    }
    private fun addData(toilets: Toilet) {
        Firebase.firestore.collection("toilets")
            .add(toilets).addOnSuccessListener {
                Log.d("SUCCESS_TAG", "Success!")
            }
            .addOnFailureListener { e ->
                Log.e("SUCCESS_TAG", "Failed! $e")
            }
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



    private fun showAddLocationDialog(markerId: String, latitude: Double, longitude: Double) {
        val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle("Add Toilet Location")
        alertDialogBuilder?.setMessage("Would you like to add this location?")
        alertDialogBuilder?.setPositiveButton("Continue") { dialog, _ ->
            //dito add data sa firebase
            var toilets = Toilet(markerId,longitude, latitude)
            addData(toilets)
            dialog.dismiss()

        }
        alertDialogBuilder!!.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        //stupid dont forget this will create the diaglog hehe
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()


    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {

            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onMapLongClick(point: Point): Boolean {
        val markerId = addMarkerAndReturnId(point)
        addViewAnnotation(point, markerId)
        return true
    }

    override fun onMapClick(point: Point): Boolean {
        map.queryRenderedFeatures(
            RenderedQueryGeometry(map.pixelForCoordinate(point)), RenderedQueryOptions(listOf(LAYER_ID), null)
        ) {
            onFeatureClicked(it) { feature ->
                if (feature.id() != null) {
                    viewAnnotationManager.getViewAnnotationByFeatureId(feature.id()!!)?.toggleViewVisibility()
                }
            }
        }
        return true
    }
    private fun onFeatureClicked(
        expected: Expected<String, List<QueriedFeature>>,
        onFeatureClicked: (Feature) -> Unit
    ) {
        if (expected.isValue && expected.value?.size!! > 0) {
            expected.value?.get(0)?.feature?.let { feature ->
                onFeatureClicked.invoke(feature)
            }
        }
    }

    private fun View.toggleViewVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    //add marker
    private fun addMarkerAndReturnId(point: Point): String {
        val currentId = "${MARKER_ID_PREFIX}${(markerId++)}"
        val toilets =Toilet(currentId,point.longitude(),point.latitude())
        //Add to firebase
        addData(toilets)
        pointList.add(Feature.fromGeometry(point, null, currentId))
        val featureCollection = FeatureCollection.fromFeatures(pointList)
        map.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
        }
        return currentId
    }


    //display all markers in map
    private fun displayMarkers(){
        getAllData("toilets") { locationList ->
            Toast.makeText(context, "${locationList.size} locations loaded.", Toast.LENGTH_SHORT).show()
            for(location in locationList){
                val point = Point.fromLngLat(location.longitude, location.latitude)
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
        const val RED_ICON_ID = "red"
        const val SOURCE_ID = "source_id"
        const val LAYER_ID = "layer_id"
        const val TERRAIN_SOURCE = "TERRAIN_SOURCE"
        const val TERRAIN_URL_TILE_RESOURCE = "mapbox://mapbox.mapbox-terrain-dem-v1"
        const val MARKER_ID_PREFIX = "view_annotation_"
        const val SELECTED_ADD_COEF_DP: Float = 8f
        const val STARTUP_TEXT = "Long click on a map to add a marker and click on a marker to pop-up annotation."
    }
}