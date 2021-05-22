package com.bebsoft.yatirimtakip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.MainActivity
import com.bebsoft.yatirimtakip.adapter.SymbolListAdapter
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.databinding.FragmentSymbolListBinding
import com.bebsoft.yatirimtakip.helper.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException

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
                    val url = context?.let {
                        Helper.getConfigValue(it, "stock_table_url")
                    }
                    val document = Jsoup.connect(url).get()
                    val tableData = document.select("tbody.tbody-type-default").select("tr")
                    val tableSize = tableData.size

                    for (i in 0 until tableSize) {
                        val symbolWithDesc = tableData.eq(i).select("td").select("strong.mr-4").text()
                        val symbol = symbolWithDesc.split(" ")
                        val value = tableData.eq(i).select("tr").select("td.text-center").eq(1).text()

                        symbolNameLivePriceHashMap[symbol[0]] = value
                    }

                    symbolListAdapter.symbolNameLivePriceHashMap = symbolNameLivePriceHashMap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                symbolListAdapter.notifyDataSetChanged()
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