package com.bebsoft.yatirimtakip

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.bebsoft.yatirimtakip.adapter.BuySellListAdapter
import com.bebsoft.yatirimtakip.adapter.SymbolListAdapter
import com.bebsoft.yatirimtakip.database.BuySell
import com.bebsoft.yatirimtakip.database.InvestDatabase
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var symbolListAdapter: SymbolListAdapter
    private lateinit var buySellListAdapter: BuySellListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        DataProvider.setDatabase(InvestDatabase.getInstance(applicationContext))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_symbol, menu)
        menuInflater.inflate(R.menu.menu_add_buy_sell, menu)
        menuInflater.inflate(R.menu.menu_see_total, menu)
        menuInflater.inflate(R.menu.menu_export, menu)
        menuInflater.inflate(R.menu.menu_import, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_symbol -> {
                addSymbolDialog()
            }
            R.id.action_add_buy_sell -> {
                //TODO
            }
            R.id.action_see_total -> {
                showTotalInvestmentDialog()
            }
            R.id.action_export -> {
                //TODO
            }
            R.id.action_import -> {
                //TODO
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addSymbolDialog() {
        val mDialogView = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_add_symbol, null, false)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Sembol Ekle")
        val mAlertDialog = mBuilder.show()

        mDialogView.findViewById<Button>(R.id.dialogAddBtn).setOnClickListener {
            mAlertDialog.dismiss()

            val symbolName = mDialogView.findViewById<EditText>(R.id.etDialogAddSymbol).text.toString()

            val insertedID = DataProvider.addSymbol(symbolName)

            var symbol = Symbol()
            symbol.symbolID = insertedID
            symbol.symbolName = symbolName
            symbolListAdapter.symbolList.add(symbol)
            symbolListAdapter.notifyDataSetChanged()
        }
        mDialogView.findViewById<Button>(R.id.dialogCancelBtn).setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun showTotalInvestmentDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Anapara")
        alertDialog.setMessage(DataProvider.getTotalInvestment())
        alertDialog.setCancelable(true)
        alertDialog.setNeutralButton("Tamam") { _, _ ->  }
        alertDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun setSymbolListAdapter(sla : SymbolListAdapter) {
        symbolListAdapter = sla
    }

    fun setBuySellListAdapter(bsla : BuySellListAdapter) {
        buySellListAdapter = bsla
    }
}