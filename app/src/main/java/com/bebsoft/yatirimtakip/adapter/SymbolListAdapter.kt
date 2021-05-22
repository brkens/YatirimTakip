package com.bebsoft.yatirimtakip.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.R
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.fragment.SymbolListFragmentDirections
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SymbolListAdapter(
    var symbolList: MutableList<Symbol>,
    var symbolNameMeanValueHashMap: HashMap<String, String>,
    var symbolNameTotalPiecesHashMap: HashMap<String, Int>,
    var symbolNameLivePriceHashMap: HashMap<String, String>,
    private val navController: NavController
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
            val meanVal = symbolNameMeanValueHashMap[curItem.symbolName]
            findViewById<TextView>(R.id.tvSymbolListSymbol).text = curItem.symbolName

            val tvProfitLoss = findViewById<TextView>(R.id.tvProfitLoss)

            val symbolVal = symbolNameLivePriceHashMap[curItem.symbolName]
            if (!symbolVal.isNullOrEmpty() && !(meanVal.isNullOrEmpty() || meanVal == Constants.emptyString)) {
                val profitLoss = (symbolVal.toBigDecimal() - meanVal.toBigDecimal()) * (symbolNameTotalPiecesHashMap[curItem.symbolName]?.toBigDecimal()!!)
                val totalProfitLossStr: String
                when {
                    profitLoss > BigDecimal(0) -> {
                        totalProfitLossStr = "+$profitLoss"
                        tvProfitLoss.setTextColor(Color.rgb(123, 159, 46))
                    }
                    profitLoss < BigDecimal(0) -> {
                        totalProfitLossStr = profitLoss.toString()
                        tvProfitLoss.setTextColor(Color.RED)
                    }
                    else -> {
                        totalProfitLossStr = "0"
                        tvProfitLoss.setTextColor(Color.BLACK)
                    }
                }
                tvProfitLoss.text = totalProfitLossStr
            }

            findViewById<TextView>(R.id.tvSymbolListMean).text = meanVal
        }

        holder.itemView.setOnClickListener {
            val action = SymbolListFragmentDirections
                .actionSymbolListFragmentToBuySellListFragment(curItem.symbolName)
            navController.navigate(action)
        }

        holder.itemView.setOnLongClickListener { view ->
            val item: TextView = view.findViewById(R.id.tvSymbolListSymbol) as TextView

            val areYouSureStr = " sembolünü ve bu sembolle alakalı tüm alım satım kayıtlarını silmek istiyor musunuz?"
            val message: String = item.text.toString() + areYouSureStr

            val alert = AlertDialog.Builder(parentContext)
            alert.setTitle(R.string.are_you_sure)
            alert.setMessage(message)
            alert.setPositiveButton(R.string.yes_text) {_, _ ->
                GlobalScope.launch {
                    DataProvider.deleteSymbol(item.text.toString())
                }

                symbolList.removeIf{
                        x: Symbol ->
                    x.symbolName == item.text.toString()
                }

                notifyDataSetChanged()
            }
            alert.setNegativeButton(R.string.no_text) {_, _: Int -> }
            alert.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return symbolList.size
    }
}