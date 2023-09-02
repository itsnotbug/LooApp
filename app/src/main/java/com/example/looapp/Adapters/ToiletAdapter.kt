package com.example.looapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.looapp.Model.Toilet
import com.example.looapp.databinding.RestroomItemLayoutBinding

class ToiletAdapter(var toiletList:MutableList<Toilet>):RecyclerView.Adapter<ToiletViewHolder>(){

fun updateData(newList: List<Toilet>) {
    toiletList.clear()
    toiletList.addAll(newList)
    notifyDataSetChanged()
}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestroomItemLayoutBinding.inflate(inflater,parent,false)
        return ToiletViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return toiletList.size
    }
    override fun onBindViewHolder(holder: ToiletViewHolder, position: Int) {
      holder.bind(toiletList[position])
    }
}