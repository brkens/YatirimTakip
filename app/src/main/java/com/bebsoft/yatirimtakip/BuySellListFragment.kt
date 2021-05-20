package com.bebsoft.yatirimtakip

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.adapter.BuySellListAdapter
import com.bebsoft.yatirimtakip.databinding.FragmentBuysellListBinding

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
            binding.tvTotalPiecesNumber.text = DataProvider.getTotalPieces(args.currentSymbol).toString()
            binding.tvTotalCostValue.text = DataProvider.getTotalCost(args.currentSymbol)

            val buySellList = DataProvider.getBuySellRecords(args.currentSymbol) as MutableList
            buySellListAdapter = BuySellListAdapter(buySellList)
            binding.rvBuySellList.adapter = buySellListAdapter
            binding.rvBuySellList.layoutManager = LinearLayoutManager(context)

            (activity as MainActivity).setBuySellListAdapter(buySellListAdapter)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}