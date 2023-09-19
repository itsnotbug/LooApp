package com.example.looapp.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.looapp.Fragments.ContributeFragment
import com.example.looapp.Model.RestroomItem
import com.example.looapp.R
import com.example.looapp.ToiletDetails
import com.example.looapp.databinding.RestroomItemLayoutBinding

class RestroomAdapter(var restroomList:MutableList<RestroomItem>):RecyclerView.Adapter<RestroomViewHolder>(){
fun updateData(newList: MutableList<RestroomItem>) {
    restroomList.clear()
    restroomList.addAll(newList)
    notifyDataSetChanged()
}
    fun setFiltered(filterList:MutableList<RestroomItem>){
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
        holder.itemView.setOnClickListener{
            var myIntent = Intent(holder.itemView.context, ToiletDetails::class.java)
            myIntent.putExtra("name",restroomList[position].name)
            myIntent.putExtra("street",restroomList[position].street)
            myIntent.putExtra("city",restroomList[position].city)
            myIntent.putExtra("state",restroomList[position].state)
            myIntent.putExtra("country",restroomList[position].country)
            myIntent.putExtra("accessible",restroomList[position].accessible)
            myIntent.putExtra("unisex",restroomList[position].unisex)
            myIntent.putExtra("comment",restroomList[position].comment)
            myIntent.putExtra("directions",restroomList[position].directions)
            myIntent.putExtra("approved",restroomList[position].approved)
            holder.itemView.context.startActivity(myIntent)
        }
    }
}