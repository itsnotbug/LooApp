package com.example.looapp.Adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Model.RestroomItem
import com.example.looapp.databinding.RestroomItemLayoutBinding

class RestroomViewHolder(var binding: RestroomItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(restroom: RestroomItem){
        binding.txtLandMark.text = restroom.name
        binding.txtLocation.text = "${restroom.street}, ${restroom.city}, ${restroom.country}"

    }
}