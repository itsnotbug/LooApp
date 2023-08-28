package com.example.looapp.Fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.looapp.LocationPermissionHelper
import com.example.looapp.Model.Toilet
import com.example.looapp.R
import com.example.looapp.databinding.FragmentExploreBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener


class ExploreFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var binding: FragmentExploreBinding
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExploreBinding.inflate(layoutInflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            map = mapView.getMapboxMap()

            getAllData("toilets") { locationList ->
                Toast.makeText(context, "${locationList.size} locations loaded.", Toast.LENGTH_SHORT).show()
             addAnnotationsToMap(locationList)
//                if (locationList.isNotEmpty()) {
//                    val cameraOptions = CameraOptions.Builder()
//                        .center(Point.fromLngLat(locationList[0].longitude, locationList[0].latitude))
//                        .zoom(9.0)
//                        .build()
//                    map?.setCamera(cameraOptions)
//                }

            }
            setMapLongClick()
        }


        // Inflate the layout for this fragment
        return binding.root
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
                    val newStudentList = Toilet(
                        document.data["longitude"].toString().toDouble(),
                        document.data["latitude"].toString().toDouble(),
                    )
                    locationList.add(newStudentList)
                }
                callback(locationList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error Occurred!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addAnnotationsToMap(location: MutableList<Toilet>) {
        Log.d("AddAnnotations", "Adding annotations to the map")

        context?.let {
            bitmapFromDrawableRes(it, R.drawable.red_marker)?.let { iconBitmap ->
                val annotationApi = mapView?.annotations
                val pointAnnotationManager = annotationApi?.createPointAnnotationManager()

                for (toiletLatLng in location) {
                    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(toiletLatLng.longitude, toiletLatLng.latitude))
                        .withIconImage(iconBitmap)
                        .withIconSize(1.0)
                        .withDraggable(true)

                    val draggableAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
                    if (draggableAnnotation != null) {
                        Log.d("AddAnnotations", "Annotation added: $toiletLatLng")
                    } else {
                        Log.d("AddAnnotations", "Failed to add annotation: $toiletLatLng")
                    }
                    // Adjust the camera position to show all annotations
//                    if (location.isNotEmpty()) {
//                        val cameraOptions = CameraOptions.Builder()
//                            .center(Point.fromLngLat(location[0].longitude, location[0].latitude))
//                            .zoom(8.0)
//                            .build()
//                        map?.setCamera(cameraOptions)
//                    }
                }
            }
        }
    }

    private fun setMapLongClick() {
        map.addOnMapLongClickListener { point ->
            context?.let {
                bitmapFromDrawableRes(it, R.drawable.red_marker)?.let { bitmap ->
                    val annotationApi = mapView?.annotations
                    val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
                    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(point.longitude(),point.latitude()))
                        .withIconImage(bitmap)

                    val formattedLatLng = "Lat: %.5f, Long: %.5f".format(point.latitude(), point.longitude())
                    val draggableAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)
                    draggableAnnotation?.isDraggable=true

                    //Display dialog
                    showAddLocationDialog(point.longitude(),point.latitude())
                   Toast.makeText(context,"$formattedLatLng",Toast.LENGTH_SHORT).show()

                    true  // Return true to consume the event
                } ?: false // Return false if the bitmap creation failed
            } ?: false // Return false if the context is null
        }
    }
     private fun showAddLocationDialog(latitude:Double,longitude: Double){
        val alertDialogBuilder = context?.let { AlertDialog.Builder(it) }
         alertDialogBuilder?.setTitle("Add Toilet Location")
         alertDialogBuilder?.setMessage("Would you like to add this location?")
         alertDialogBuilder?.setPositiveButton("Continue"){dialog, _->
             //dito add data sa firebase
             var toilets = Toilet(longitude,latitude)
             addData(toilets)
             dialog.dismiss()

         }
         alertDialogBuilder!!.setNegativeButton("Cancel"){dialog,_->
             dialog.dismiss()
         }
         //stupid dont forget this will create the diaglog hehe
         val alertDialog: AlertDialog =alertDialogBuilder.create()
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

}