package com.example.looapp.Screens

import android.os.Bundle
import android.util.Log
import android.widget.CheckedTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.looapp.R
import com.example.looapp.databinding.ActivityRestroomRatingsBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point

class RestroomRatings : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityRestroomRatingsBinding
    private lateinit var checkedTextViewsCollection: CollectionReference
    private val checkedTextViews = mutableListOf<CheckedTextView>()
    private lateinit var coordinate: Point


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_restroom_ratings)
        binding.lifecycleOwner = this

        val longitude = intent.extras!!.getDouble("longitude")
        val latitude = intent.extras!!.getDouble("latitude")
        val markerId = intent.extras!!.getString("markerId")

        firestore = FirebaseFirestore.getInstance()
        checkedTextViewsCollection = firestore.collection("checkedTextViewData")
        val emojiImageView = binding.imgEmojiRate
        val item2 = binding.checkedTextView2
        val item3 = binding.checkedTextView3
        val item4 = binding.checkedTextView4
        val item5 = binding.checkedTextView5
        val item6 = binding.checkedTextView6
        val item7 = binding.checkedTextView7
        val item8 = binding.checkedTextView8
        val item9 = binding.checkedTextView9
        val item10 = binding.checkedTextView10

        checkedTextViews.add(item2)
        checkedTextViews.add(item3)
        checkedTextViews.add(item4)
        checkedTextViews.add(item5)
        checkedTextViews.add(item6)
        checkedTextViews.add(item7)
        checkedTextViews.add(item8)
        checkedTextViews.add(item9)
        checkedTextViews.add(item10)

        if (item2.isChecked) {
            toggleCheckedState(item2)
        }
        // Set click listeners for each CheckedTextView
        for (checkedTextView in checkedTextViews) {
            checkedTextView.setOnClickListener {
                // Toggle the checked state
                val isChecked = checkedTextView.isChecked
                checkedTextView.isChecked = !isChecked
                // Toggle checked state color
                toggleCheckedState(checkedTextView)
            }
        }
        val submit = binding.btnSubmit
        val ratingOne = binding.ratingBarOne
        val ratingTwo = binding.ratingBarTwo
        val ratingBar = binding.ratingBarOverAll

        submit.setOnClickListener {

            // Create a list of CheckedTextView elements that you want to use to update properties
            val checkedTextViewsToUpdate = listOf(
                item2,
                item3,
                item4,
                item5,
                item6,
                item7,
                item8,
                item9,
                item10
            )
            // Get the ratings
            val oneRating = ratingOne.rating.toString()
            val twoRating = ratingTwo.rating.toString()
            val overAllRating = ratingBar.rating.toString()
            val comments = binding.txtTellUsMore.toString()

            // Update both checked data and ratings data in Firestore
            updateDataAndRatingsToFirestore(markerId,longitude,latitude,oneRating,twoRating,overAllRating,checkedTextViewsToUpdate,comments)
        }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            // Update the emoji drawable based on the rating
            val emojiDrawableId = when (rating) {
                1.0f -> R.drawable.sad
                2.0f -> R.drawable.worried
                3.0f -> R.drawable.relieved
                4.0f -> R.drawable.glad
                5.0f -> R.drawable.fullfilled
                else -> R.drawable.glad
            }
            emojiImageView.setImageResource(emojiDrawableId)
        }
    }



    private fun toggleCheckedState(checkedTextView: CheckedTextView) {
        // Apply the custom layout as the background when checked
        val drawableId = if (checkedTextView.isChecked) {
            R.drawable.custom_item_changed_layout // Change to your custom layout resource
        } else {
            R.drawable.custom_border_ratings // Use the original layout for unchecked state
        }
        checkedTextView.setBackgroundResource(drawableId)

        // Apply text color based on the selected or default state
        val textColorId = if (checkedTextView.isChecked) {
            R.color.white // Change to your desired checked text color
        } else {
            R.color.colorPrimary // Use the original text color for unchecked state
        }
        checkedTextView.setTextColor(resources.getColor(textColorId))
    }
    private fun updateDataAndRatingsToFirestore(
        markerId: String?,
        longitude:Double?,
        latitude: Double?,
        oneRating: String,
        twoRating: String,
        overAllRating: String,
        checkedTextViews: List<CheckedTextView>,
        comments:String
    ) {
        val cleanliness = oneRating.toFloatOrNull()
        val maintenance = twoRating.toFloatOrNull()
        val overallExperience = overAllRating.toFloatOrNull()

        // Create a map with the checked states data
        val checkedData = mapOf(
            "isHygenic" to checkedTextViews[0].isChecked,
            "isFreeOfUse" to checkedTextViews[1].isChecked,
            "isMaintained" to checkedTextViews[2].isChecked,
            "isComfy" to checkedTextViews[3].isChecked,
            "hasBidet" to checkedTextViews[4].isChecked,
            "hasSink" to checkedTextViews[5].isChecked,
            "hasTrashBin" to checkedTextViews[6].isChecked,
            "hasToiletTries" to checkedTextViews[7].isChecked,
            "hasHandDryer" to checkedTextViews[8].isChecked
        )

        // Create a map with the ratings data
        val ratingsData = mapOf(
            "markerId" to markerId,
            "longitude" to longitude,
            "latitude" to latitude,
            "cleanliness" to cleanliness,
            "maintenance" to maintenance,
            "overAllRating" to overallExperience
        )

        // Combine checked data and ratings data into a single map
        val combinedData = mapOf(
            "checkedData" to checkedData,
            "ratingsData" to ratingsData
        )

        // Update the data in Firestore with the combined map
        firestore.collection("ratings")
            .add(combinedData)
            .addOnSuccessListener { documentReference ->
                Log.d("SUCCESS_TAG", "Data Added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("SUCCESS_TAG", "Failed to Add Data! $e")
            }
    }
}

