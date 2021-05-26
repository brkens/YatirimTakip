package com.bebsoft.yatirimtakip.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.MainActivity
import com.bebsoft.yatirimtakip.adapter.SymbolListAdapter
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.databinding.FragmentSymbolListBinding
import com.bebsoft.yatirimtakip.helper.ConfigHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SymbolListFragment : Fragment() {

    private var _binding: FragmentSymbolListBinding? = null

    private val binding get() = _binding!!

    private lateinit var symbolListAdapter: SymbolListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSymbolListBinding.inflate(inflater, container, false)

        val navController = findNavController()
        var symbolList = mutableListOf<Symbol>()
        var symbolNameMeanValueHashMap = hashMapOf<String, String>()
        var symbolNameTotalPiecesHashMap = hashMapOf<String, Int>()
        val symbolNameLivePriceHashMap = hashMapOf<String, String>()

        symbolListAdapter = SymbolListAdapter(symbolList, symbolNameMeanValueHashMap,
            symbolNameTotalPiecesHashMap, symbolNameLivePriceHashMap, navController)
        binding.rvSymbolList.adapter = symbolListAdapter
        binding.rvSymbolList.layoutManager = LinearLayoutManager(context)

        GlobalScope.launch {
            symbolList = DataProvider.getSymbolList() as MutableList<Symbol>
            symbolListAdapter.symbolList = symbolList

            symbolNameMeanValueHashMap = DataProvider.getAllMeanValuesMap()
            symbolListAdapter.symbolNameMeanValueHashMap = symbolNameMeanValueHashMap

            symbolNameTotalPiecesHashMap = DataProvider.getAllTotalPiecesMap()
            symbolListAdapter.symbolNameTotalPiecesHashMap = symbolNameTotalPiecesHashMap

            kotlin.runCatching {
                try {
                    val stockUrl = context?.let {
                        ConfigHelper.getConfigValue(it, "stock_table_url")
                    }
                    val stockDocument = Jsoup.connect(stockUrl).get()
                    val stockTableData = stockDocument.select("tbody.tbody-type-default").select("tr")
                    val stockTableSize = stockTableData.size
                    for (i in 0 until stockTableSize) {
                        val symbolWithDesc = stockTableData.eq(i).select("td").select("strong.mr-4").text()
                        val symbol = symbolWithDesc.split(" ")
                        val value = stockTableData.eq(i).select("tr").select("td.text-center").eq(1).text()
                        symbolNameLivePriceHashMap[symbol[0]] = value
                    }

                    val fxUrl = context?.let {
                        ConfigHelper.getConfigValue(it, "fx_table_url")
                    }
                    val fxDocument = Jsoup.connect(fxUrl).get()
                    val fxTableData = fxDocument.select("table.table-detail").select("tbody").select("tr")
                    for (i in 0 until 3) {
                        val elements = fxTableData.eq(i).select("td")
                        val currencySymbolStr = elements.select("td.text-title").select("a").text()
                        val currencySymbolStrArr = currencySymbolStr.split(" ")
                        val currencySymbol = currencySymbolStrArr[0]
                        val currencyValueStr = elements.select("td.text-right").select("div").eq(0).text()
                        val currencyValue = currencyValueStr.toBigDecimal().setScale(2, RoundingMode.DOWN).toString()
                        symbolNameLivePriceHashMap[currencySymbol] = currencyValue
                    }

                    symbolListAdapter.symbolNameLivePriceHashMap = symbolNameLivePriceHashMap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            var overAllTotalProfitLoss = BigDecimal(0)
            for (symbol in symbolNameLivePriceHashMap.keys) {
                val meanVal = symbolNameMeanValueHashMap[symbol]
                val symbolVal = symbolNameLivePriceHashMap[symbol]
                if (!symbolVal.isNullOrEmpty() && !(meanVal.isNullOrEmpty() || meanVal == Constants.EMPTY_STRING)) {
                    val profitLoss = (symbolVal.toBigDecimal() - meanVal.toBigDecimal()) * (symbolNameTotalPiecesHashMap[symbol]?.toBigDecimal()!!)
                    overAllTotalProfitLoss = overAllTotalProfitLoss.add(profitLoss)
                }
            }

            withContext(Dispatchers.Main) {
                symbolListAdapter.notifyDataSetChanged()

                binding.tvSymvolListTotalCostValue.text = overAllTotalProfitLoss.setScale(2, RoundingMode.UP).toString()
                when {
                    overAllTotalProfitLoss > BigDecimal(0) -> {
                        binding.tvSymvolListTotalCostValue.setTextColor(Color.rgb(123, 159, 46))
                    }
                    overAllTotalProfitLoss < BigDecimal(0) -> {
                        binding.tvSymvolListTotalCostValue.setTextColor(Color.RED)
                    }
                    else -> {
                        binding.tvSymvolListTotalCostValue.setTextColor(Color.BLACK)
                    }
                }
            }
        }

        (activity as MainActivity).setSymbolListAdapter(symbolListAdapter)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}