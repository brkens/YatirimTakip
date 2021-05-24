package com.bebsoft.yatirimtakip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bebsoft.yatirimtakip.adapter.BuySellListAdapter
import com.bebsoft.yatirimtakip.adapter.SymbolListAdapter
import com.bebsoft.yatirimtakip.database.BuySell
import com.bebsoft.yatirimtakip.database.DataProvider
import com.bebsoft.yatirimtakip.database.InvestDatabase
import com.bebsoft.yatirimtakip.database.Symbol
import com.bebsoft.yatirimtakip.databinding.ActivityMainBinding
import com.bebsoft.yatirimtakip.fragment.BuySellListFragmentDirections
import com.bebsoft.yatirimtakip.fragment.SymbolListFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var symbolListAdapter: SymbolListAdapter
    private lateinit var buySellListAdapter: BuySellListAdapter

    private lateinit var buySellDialogView: View

    private var selectedBuySellSymbol = Constants.emptyString

    private var day = 0
    private var month: Int = 0
    private var year: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var myDay = 0
    private var myMonth: Int = 0
    private var myYear: Int = 0
    private var myHour: Int = 0
    private var myMinute: Int = 0

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
        menuInflater.inflate(R.menu.menu_see_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_symbol -> {
                addSymbolDialog()
            }
            R.id.action_add_buy_sell -> {
                if (selectedBuySellSymbol == Constants.emptyString) {
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle(R.string.warning)
                    alertDialog.setMessage(R.string.select_symbol_message)
                    alertDialog.setCancelable(false)
                    alertDialog.setNeutralButton(R.string.ok_text) { _, _ ->  }
                    alertDialog.show()
                } else {
                    addBuySellDialog()
                }
            }
            R.id.action_see_total -> {
                showTotalInvestmentDialog()
            }
            R.id.action_see_history -> {
                val symbolListToHistoryAction = SymbolListFragmentDirections.actionSymbolListFragmentToHistoryFragment()
                val buySellListToHistoryAction = BuySellListFragmentDirections.actionBuySellListFragmentToHistoryFragment()
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                val destId = navController.currentDestination?.id
                if (destId == R.id.SymbolListFragment) {
                    navController.navigate(symbolListToHistoryAction)
                } else if (destId == R.id.BuySellListFragment){
                    navController.navigate(buySellListToHistoryAction)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addSymbolDialog() {
        val symbolDialogView = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_add_symbol, null, false)
        val symbolDialogBuilder = AlertDialog.Builder(this).setView(symbolDialogView).setTitle(R.string.add_symbol)
        val symbolAlertDialog = symbolDialogBuilder.show()

        symbolDialogView.findViewById<Button>(R.id.btnDialogSymbolAdd).setOnClickListener {
            symbolAlertDialog.dismiss()

            val symbolName = symbolDialogView.findViewById<EditText>(R.id.etDialogSymbolAdd).text.toString()

            GlobalScope.launch {
                if (DataProvider.symbolExists(symbolName)) {
                    withContext(Dispatchers.Main) {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle(R.string.attention)
                        alertDialog.setMessage(R.string.symbol_already_exists)
                        alertDialog.setCancelable(true)
                        alertDialog.setNeutralButton(R.string.ok_text) { _, _ -> }
                        alertDialog.show()
                    }
                } else {
                    val insertedID = DataProvider.addSymbol(symbolName)

                    val symbol = Symbol()
                    symbol.symbolID = insertedID
                    symbol.symbolName = symbolName
                    symbolListAdapter.symbolList.add(symbol)

                    withContext(Dispatchers.Main) {
                        symbolListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        symbolDialogView.findViewById<Button>(R.id.btnDialogSymbolCancel).setOnClickListener {
            symbolAlertDialog.dismiss()
        }
    }

    private fun addBuySellDialog() {
        val nullParent : ViewGroup? = null
        buySellDialogView = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_add_buysell, nullParent, false)
        val buySellDialogBuilder = AlertDialog.Builder(this).setView(buySellDialogView).setTitle(R.string.add_buy_sell)
        val buySellAlertDialog = buySellDialogBuilder.show()

        buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellSymbol).text = selectedBuySellSymbol

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())
        buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellDateTime).text = currentDate

        buySellDialogView.findViewById<Button>(R.id.btnDateTimePicker)?.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this@MainActivity, this@MainActivity, year, month,day)
            datePickerDialog.show()
        }

        buySellDialogView.findViewById<Button>(R.id.btnDialogBuySellAdd).setOnClickListener {
            buySellAlertDialog.dismiss()

            val pieces = buySellDialogView.findViewById<EditText>(R.id.etPieces).text.toString()
            val value = buySellDialogView.findViewById<EditText>(R.id.etValue).text.toString()
            val totalCost = pieces.toBigDecimal().multiply(value.toBigDecimal())
            val dateTime = buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellDateTime).text.toString()

            GlobalScope.launch {
                val buySell = BuySell()
                buySell.pieces = pieces.toLong()
                buySell.value = value
                buySell.totalCost = totalCost.toString()
                buySell.dateTime = dateTime
                buySell.fkSymbolID = DataProvider.getSymbolId(selectedBuySellSymbol)

                val insertedID = DataProvider.addBuySell(buySell)

                buySell.recordID = insertedID
                buySellListAdapter.buySellList.add(buySell)

                val newTotalPieces = DataProvider.getTotalPieces(selectedBuySellSymbol).toString()
                val newTotalCost = DataProvider.getTotalCost(selectedBuySellSymbol)

                withContext(Dispatchers.Main) {
                    buySellListAdapter.notifyDataSetChanged()

                    findViewById<TextView>(R.id.tvTotalPiecesNumber).text = newTotalPieces
                    findViewById<TextView>(R.id.tvTotalCostValue).text = newTotalCost
                }
            }
        }
        buySellDialogView.findViewById<Button>(R.id.btnDialogBuySellCancel).setOnClickListener {
            buySellAlertDialog.dismiss()
        }
    }

    private fun showTotalInvestmentDialog() {
        GlobalScope.launch {
            val message = DataProvider.getTotalInvestment()

            withContext(Dispatchers.Main) {
                val alertDialog = AlertDialog.Builder(this@MainActivity)
                alertDialog.setTitle(R.string.capital)
                alertDialog.setMessage(message)
                alertDialog.setCancelable(true)
                alertDialog.setNeutralButton(R.string.ok_text) { _, _ -> }
                alertDialog.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        selectedBuySellSymbol = Constants.emptyString

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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month + 1
        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this@MainActivity, this@MainActivity, hour, minute,
            DateFormat.is24HourFormat(this))
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        val txt = myYear.toString() + "-" + myMonth.toString().padStart(2, '0') + "-" + myDay.toString().padStart(2, '0') + " " + myHour.toString().padStart(2, '0') + ":" + myMinute.toString().padStart(2, '0')
        buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellDateTime).text = txt
    }

    fun setSelectedBuySellSymbol(selectedSymbol: String) {
        selectedBuySellSymbol = selectedSymbol
    }
}