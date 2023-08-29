package com.elio.listpreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ListAdapter(firebaseList: ArrayList<String>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private  var feedItemList : List <String> = firebaseList

    //the main view that holds the adapter's layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.list_adapter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedItem = listOf(feedItemList[position])
        for (i in feedItem.indices) {
            holder.txtValue.text = feedItem[i].toString()
        }

    }

    override fun getItemCount(): Int {
        return feedItemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // declaring the widgets
        internal var txtValue: TextView

        init {
            txtValue = itemView.findViewById(R.id.txtValue)
        }

    }

}
