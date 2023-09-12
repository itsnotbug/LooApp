package com.example.looapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Model.Restroom
import com.example.looapp.databinding.RestroomItemLayoutBinding

class RestroomAdapter(var restroomList:MutableList<Restroom>):RecyclerView.Adapter<RestroomViewHolder>(){
fun updateData(newList: List<Restroom>) {
    restroomList.clear()
    restroomList.addAll(newList)
    notifyDataSetChanged()
}
    fun setFiltered(filterList:MutableList<Restroom>){
        this.restroomList =filterList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestroomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestroomItemLayoutBinding.inflate(inflater,parent,false)
        return RestroomViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return restroomList.size
    }
    override fun onBindViewHolder(holder: RestroomViewHolder, position: Int) {
      holder.bind(restroomList[position])
    }
}