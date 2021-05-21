package com.bebsoft.yatirimtakip.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.MainActivity
import com.bebsoft.yatirimtakip.adapter.BuySellListAdapter
import com.bebsoft.yatirimtakip.database.BuySell
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.databinding.FragmentBuysellListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BuySellListFragment : Fragment() {

    private var _binding: FragmentBuysellListBinding? = null

    private val binding get() = _binding!!

    private lateinit var buySellListAdapter : BuySellListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBuysellListBinding.inflate(inflater, container, false)

        arguments?.let {
            val args = BuySellListFragmentArgs.fromBundle(it)

            binding.tvBuySellFragmentSymbolName.text = args.currentSymbol

            GlobalScope.launch {
                val totalPiecesStr = DataProvider.getTotalPieces(args.currentSymbol).toString()
                val totalCostStr = DataProvider.getTotalCost(args.currentSymbol)

                withContext(Dispatchers.Main) {
                    binding.tvTotalPiecesNumber.text = totalPiecesStr
                    binding.tvTotalCostValue.text = totalCostStr
                }
            }

            var buySellList = mutableListOf<BuySell>()
            buySellListAdapter = BuySellListAdapter(buySellList)
            binding.rvBuySellList.adapter = buySellListAdapter
            binding.rvBuySellList.layoutManager = LinearLayoutManager(context)

            GlobalScope.launch {
                buySellList = DataProvider.getBuySellRecords(args.currentSymbol) as MutableList
                buySellListAdapter.buySellList = buySellList

                withContext(Dispatchers.Main) {
                    buySellListAdapter.notifyDataSetChanged()
                }
            }

            (activity as MainActivity).setSelectedBuySellSymbol(args.currentSymbol)
            (activity as MainActivity).setBuySellListAdapter(buySellListAdapter)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}