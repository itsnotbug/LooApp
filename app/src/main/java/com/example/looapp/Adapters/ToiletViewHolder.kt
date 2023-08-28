package com.example.looapp.Adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Model.Toilet
import com.example.looapp.databinding.RestroomItemLayoutBinding

class ToiletViewHolder(var binding: RestroomItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(toilet: Toilet){
        binding.txtLandMark.text = toilet.latitude.toString()
        binding.txtLocation.text = toilet.longitude.toString()

    }
}