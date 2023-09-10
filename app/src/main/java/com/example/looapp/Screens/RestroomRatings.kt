package com.example.looapp.Screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckedTextView
import androidx.databinding.DataBindingUtil
import com.example.looapp.R
import com.example.looapp.databinding.ActivityRestroomRatingsBinding
import com.google.android.gms.common.util.DataUtils

class RestroomRatings : AppCompatActivity() {
    private  lateinit var binding: ActivityRestroomRatingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_restroom_ratings)
        binding.lifecycleOwner=this

        val ratingBar = binding.ratingBarOverAll
        val emojiImageView = binding.imgEmojiRate
        val item2 = binding.checkedTextView2
        val item3 = binding.checkedTextView3
        val item4 = binding.checkedTextView4
        val item5 = binding.checkedTextView5
        val item6 = binding.checkedTextView6
        val item7 = binding.checkedTextView7
        val item8 = binding.checkedTextView8
        val item9 =  binding.checkedTextView9
        val item10 = binding.checkedTextView10

        item2.setOnClickListener {
            toggleCheckedState(item2)
        }

        item3.setOnClickListener {
            toggleCheckedState(item3)
        }
        item4.setOnClickListener {
            toggleCheckedState(item4)
        }
        item5.setOnClickListener {
            toggleCheckedState(item5)
        }
        item6.setOnClickListener {
            toggleCheckedState(item6)
        }
        item7.setOnClickListener {
            toggleCheckedState(item7)
        }
        item8.setOnClickListener {
            toggleCheckedState(item8)
        }
        item9.setOnClickListener {
            toggleCheckedState(item9)
        }
        item10.setOnClickListener {
            toggleCheckedState(item10)
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
        checkedTextView.isChecked = !checkedTextView.isChecked
        // Apply the custom color selector drawable as the background
        val drawableId = if (checkedTextView.isChecked) R.drawable.custom_item_changed_layout else R.drawable.custom_border_ratings
        checkedTextView.setBackgroundResource(drawableId)

        // Apply text color based on the selected or default state
        val textColorId = if (checkedTextView.isChecked) R.color.white else R.color.colorPrimary
        checkedTextView.setTextColor(resources.getColor(textColorId))
    }
}