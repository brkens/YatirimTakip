package com.bebsoft.yatirimtakip.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.R
import com.bebsoft.yatirimtakip.database.BuySell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val parentActivity = (parentContext as Activity)

        try {
            val currentSymbol = parentActivity.findViewById<TextView>(
                R.id.tvBuySellFragmentSymbolName)?.text.toString()

            holder.itemView.apply {
                findViewById<TextView>(R.id.tvBuySellListPieces).text = curItem.pieces.toString()
                findViewById<TextView>(R.id.tvBuySellListValue).text = curItem.value
                findViewById<TextView>(R.id.tvBuySellListDateTime).text = curItem.dateTime
                findViewById<TextView>(R.id.tvBuySellListTotalCost).text = curItem.totalCost
            }

            holder.itemView.setOnLongClickListener {
                try {
                    val message = "Al??m sat??m kayd??n?? silmek istiyor musunuz?"

                    val alert = AlertDialog.Builder(parentContext)
                    alert.setTitle(R.string.are_you_sure)
                    alert.setMessage(message)
                    alert.setPositiveButton(R.string.yes_text) { _, _ ->
                        try {
                            GlobalScope.launch {
                                kotlin.runCatching {
                                    DataProvider.deleteBuySellRecord(curItem.recordID)
                                    val newTotalPieces = DataProvider.getTotalPieces(currentSymbol)
                                    val newTotalCost = DataProvider.getTotalCost(currentSymbol)

                                    withContext(Dispatchers.Main) {
                                        parentActivity
                                            .findViewById<TextView>(
                                                R.id.tvTotalPiecesNumber)?.text =
                                                    newTotalPieces.toString()
                                        parentActivity
                                            .findViewById<TextView>(
                                                R.id.tvTotalCostValue)?.text =
                                                    newTotalCost
                                    }
                                }.onFailure {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            parentContext,
                                            Constants.ERROR_MESSAGE,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            buySellList.removeIf { x: BuySell ->
                                x.recordID == curItem.recordID
                            }

                            notifyDataSetChanged()
                        } catch (exc2 : Exception) {
                            Toast.makeText(parentContext,
                                Constants.ERROR_MESSAGE,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    alert.setNegativeButton(R.string.no_text) { _, _: Int -> }
                    alert.show()
                } catch (exc1 : Exception) {
                    Toast.makeText(
                        parentContext,
                        Constants.ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
                true
            }
        } catch (exc: Exception) {
            Toast.makeText(parentContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return buySellList.size
    }
}