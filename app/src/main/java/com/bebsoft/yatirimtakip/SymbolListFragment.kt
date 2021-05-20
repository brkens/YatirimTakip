package com.bebsoft.yatirimtakip

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebsoft.yatirimtakip.adapter.SymbolListAdapter
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.databinding.FragmentSymbolListBinding

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
        val symbolList = DataProvider.getSymbolList() as MutableList<Symbol>
        symbolListAdapter = SymbolListAdapter(symbolList, navController)
        binding.rvSymbolList.adapter = symbolListAdapter
        binding.rvSymbolList.layoutManager = LinearLayoutManager(context)

        (activity as MainActivity).setSymbolListAdapter(symbolListAdapter)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}