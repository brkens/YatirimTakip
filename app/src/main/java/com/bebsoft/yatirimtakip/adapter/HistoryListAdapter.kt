package com.bebsoft.yatirimtakip.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.R

class HistoryListAdapter(
    var symbolNameProfitLossHashMap: HashMap<String, String>
) : RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    private lateinit var parentContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        parentContext = parent.context

        return HistoryViewHolder(
            LayoutInflater.from(parentContext).inflate(
                R.layout.item_history_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val curItem = symbolNameProfitLossHashMap.keys.sorted()[position]

        holder.itemView.apply {
            try {
                findViewById<TextView>(
                    R.id.tvHistoryItemSymbol).text = curItem
                findViewById<TextView>(
                    R.id.tvHistoryItemProfitLoss).text = symbolNameProfitLossHashMap[curItem]
            } catch (exc: Exception) {
                Toast.makeText(parentContext, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return symbolNameProfitLossHashMap.size
    }
}