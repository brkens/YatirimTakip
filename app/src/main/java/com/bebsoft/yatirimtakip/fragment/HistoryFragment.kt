package com.bebsoft.yatirimtakip.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.R
import com.bebsoft.yatirimtakip.adapter.HistoryListAdapter
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var historyListAdapter: HistoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        var symbolProfitlossMap: HashMap<String, String> = hashMapOf()
        historyListAdapter = HistoryListAdapter(symbolProfitlossMap)
        binding.rvHistory.adapter = historyListAdapter
        binding.rvHistory.layoutManager = LinearLayoutManager(context)

        GlobalScope.launch {
            kotlin.runCatching {
                var totalProfitLoss = BigDecimal(0)

                val totalPiecesMap = DataProvider.getAllTotalPiecesMap()

                val symbolList = DataProvider.getSymbolList()
                for (symbol in symbolList) {
                    if (totalPiecesMap[symbol.symbolName] == 0) {
                        val totalCostStr = DataProvider.getTotalCost(symbol.symbolName)

                        val curTotalCost = totalCostStr.toBigDecimal().multiply(BigDecimal(-1))

                        symbolProfitlossMap[symbol.symbolName] = curTotalCost.toString()

                        totalProfitLoss = totalProfitLoss.add(curTotalCost)
                    } else {
                        symbolProfitlossMap[symbol.symbolName] = "!!!"
                    }
                }

                historyListAdapter.symbolNameProfitLossHashMap = symbolProfitlossMap

                withContext(Dispatchers.Main) {
                    binding.tvHistoryTotalValue.text = totalProfitLoss
                        .setScale(2, RoundingMode.UP).toString()

                    historyListAdapter.notifyDataSetChanged()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}