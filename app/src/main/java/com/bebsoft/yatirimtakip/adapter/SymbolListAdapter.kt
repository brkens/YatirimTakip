package com.bebsoft.yatirimtakip.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bebsoft.yatirimtakip.R
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.DataProvider
import com.bebsoft.yatirimtakip.SymbolListFragmentDirections

class SymbolListAdapter(
    val symbolList: MutableList<Symbol>, private val navController: NavController
) : RecyclerView.Adapter<SymbolListAdapter.SymbolViewHolder>() {

    class SymbolViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    private lateinit var parentContext : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymbolViewHolder {
        parentContext = parent.context

        return SymbolViewHolder(
            LayoutInflater.from(parentContext).inflate(
                R.layout.item_symbol_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SymbolViewHolder, position: Int) {
        val curItem = symbolList[position]

        holder.itemView.apply {
            findViewById<TextView>(R.id.tvSymbolListSymbol).text = curItem.symbolName
            findViewById<TextView>(R.id.tvSymbolListMean).text = DataProvider.getMeanValue(curItem.symbolName)
        }

        holder.itemView.setOnClickListener {
            val action = SymbolListFragmentDirections
                .actionSymbolListFragmentToBuySellListFragment(curItem.symbolName)
            navController.navigate(action)
        }

        holder.itemView.setOnLongClickListener { view ->
            val item: TextView = view.findViewById(R.id.tvSymbolListSymbol) as TextView

            val areYouSureStr = " sembolünü ve bu sembolle alakalı tüm alım satım kayıtlarını silmek istiyor musunuz?"
            val message: String = item.text.toString() + " " + areYouSureStr

            val alert = AlertDialog.Builder(parentContext)
            alert.setTitle("Emin misiniz?")
            alert.setMessage(message)
            alert.setPositiveButton("Evet") {_, _ ->
                DataProvider.deleteSymbol(item.text.toString())

                symbolList.removeIf{
                        x: Symbol -> x.symbolName.equals(item.text.toString())
                }

                notifyDataSetChanged()
            }
            alert.setNegativeButton("Hayır") {_, _: Int -> }
            alert.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return symbolList.size
    }
}