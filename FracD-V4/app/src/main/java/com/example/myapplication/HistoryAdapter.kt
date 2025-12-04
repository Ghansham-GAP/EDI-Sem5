package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.resultText.text = historyItem.result
        holder.dateText.text = historyItem.date
        holder.timeText.text = historyItem.time

        if (historyItem.result.contains("Fractured")) {
            holder.resultText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorError))
        } else {
            holder.resultText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorSuccess))
        }

        Glide.with(holder.itemView.context)
            .load(historyItem.imagePath)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_history_image)
        val resultText: TextView = itemView.findViewById(R.id.item_history_result)
        val dateText: TextView = itemView.findViewById(R.id.item_history_date)
        val timeText: TextView = itemView.findViewById(R.id.item_history_time)
    }
}
