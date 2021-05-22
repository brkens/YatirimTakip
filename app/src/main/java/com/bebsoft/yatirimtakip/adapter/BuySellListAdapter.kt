package com.bebsoft.yatirimtakip.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.R
import com.bebsoft.yatirimtakip.database.BuySell
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuySellListAdapter (
    var buySellList : MutableList<BuySell>
) : RecyclerView.Adapter<BuySellListAdapter.BuySellViewHolder>() {

    class BuySellViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    private lateinit var parentContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuySellViewHolder {
        parentContext = parent.context

        return BuySellViewHolder(
            LayoutInflater.from(parentContext).inflate(
                R.layout.item_buysell_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BuySellViewHolder, position: Int) {
        val curItem = buySellList[position]

        holder.itemView.apply {
            findViewById<TextView>(R.id.tvBuySellListPieces).text = curItem.pieces.toString()
            findViewById<TextView>(R.id.tvBuySellListValue).text = curItem.value
            findViewById<TextView>(R.id.tvBuySellListDateTime).text = curItem.dateTime
            findViewById<TextView>(R.id.tvBuySellListTotalCost).text = curItem.totalCost
        }

        holder.itemView.setOnLongClickListener {
            val message = "Alım satım kaydını silmek istiyor musunuz?"

            val alert = AlertDialog.Builder(parentContext)
            alert.setTitle(R.string.are_you_sure)
            alert.setMessage(message)
            alert.setPositiveButton(R.string.yes_text) {_, _ ->
                GlobalScope.launch {
                    DataProvider.deleteBuySellRecord(curItem.recordID)
                }

                buySellList.removeIf {
                        x: BuySell -> x.recordID == curItem.recordID
                }

                notifyDataSetChanged()
            }
            alert.setNegativeButton(R.string.no_text) {_, _: Int -> }
            alert.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return buySellList.size
    }
}