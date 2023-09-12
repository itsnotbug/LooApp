package com.example.looapp.Adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Model.Restroom
import com.example.looapp.databinding.RestroomItemLayoutBinding

class RestroomViewHolder(var binding: RestroomItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(restroom: Restroom){
        binding.txtLandMark.text = restroom.place
        binding.txtLocation.text = restroom.formattedAddress

    }
}