package com.bebsoft.yatirimtakip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
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
import com.bebsoft.yatirimtakip.helper.DriveServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
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
    private lateinit var driveServiceHelper: DriveServiceHelper
    private lateinit var dialogLoading: AlertDialog
    private lateinit var client: GoogleSignInClient

    private var selectedBuySellSymbol = Constants.EMPTY_STRING
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
        menuInflater.inflate(R.menu.menu_sync_with_drive, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_symbol -> {
                addSymbolDialog()
            }
            R.id.action_add_buy_sell -> {
                if (selectedBuySellSymbol == Constants.EMPTY_STRING) {
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
                try {
                    val symbolListToHistoryAction = SymbolListFragmentDirections
                        .actionSymbolListFragmentToHistoryFragment()
                    val buySellListToHistoryAction = BuySellListFragmentDirections
                        .actionBuySellListFragmentToHistoryFragment()
                    val navController = findNavController(R.id.nav_host_fragment_content_main)
                    val destId = navController.currentDestination?.id
                    if (destId == R.id.SymbolListFragment) {
                        navController.navigate(symbolListToHistoryAction)
                    } else if (destId == R.id.BuySellListFragment) {
                        navController.navigate(buySellListToHistoryAction)
                    }
                } catch (exc: Exception) {
                    Toast.makeText(applicationContext,
                        Constants.ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            R.id.action_sync_with_drive -> {
                sendToDriveDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_CODE_SIGN_IN -> {
                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data)
                }
            }
        }
    }

    private fun handleSignInIntent(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            try {
                val credential: GoogleAccountCredential = GoogleAccountCredential
                    .usingOAuth2(
                        this@MainActivity,
                        Collections.singleton(DriveScopes.DRIVE_FILE)
                    )

                credential.selectedAccount = it.account

                val googleDriveService: Drive = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("YatirimTakip")
                    .build()

                driveServiceHelper = DriveServiceHelper(googleDriveService)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }.addOnFailureListener {
            Toast.makeText(applicationContext,
                "Sign In başarısız!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun addSymbolDialog() {
        try {
            val symbolDialogView = LayoutInflater
                .from(applicationContext)
                .inflate(R.layout.dialog_add_symbol, null, false)
            val symbolDialogBuilder = AlertDialog
                .Builder(this)
                .setView(symbolDialogView)
                .setTitle(R.string.add_symbol)
            val symbolAlertDialog = symbolDialogBuilder.show()

            symbolDialogView.findViewById<Button>(R.id.btnDialogSymbolAdd).setOnClickListener {
                symbolAlertDialog.dismiss()

                val symbolName = symbolDialogView
                    .findViewById<EditText>(R.id.etDialogSymbolAdd).text.toString()

                GlobalScope.launch {
                    kotlin.runCatching {
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
                    }.onFailure {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext,
                                Constants.ERROR_MESSAGE,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

            symbolDialogView.findViewById<Button>(R.id.btnDialogSymbolCancel).setOnClickListener {
                symbolAlertDialog.dismiss()
            }
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun addBuySellDialog() {
        try {
            val nullParent: ViewGroup? = null
            buySellDialogView = LayoutInflater.from(applicationContext)
                .inflate(R.layout.dialog_add_buysell, nullParent, false)
            val buySellDialogBuilder = AlertDialog.Builder(this)
                    .setView(buySellDialogView).setTitle(R.string.add_buy_sell)
            val buySellAlertDialog = buySellDialogBuilder.show()

            buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellSymbol).text =
                selectedBuySellSymbol

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentDate = sdf.format(Date())
            buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellDateTime).text =
                currentDate

            buySellDialogView.findViewById<Button>(R.id.btnDateTimePicker)?.setOnClickListener {
                try {
                    val calendar: Calendar = Calendar.getInstance()
                    day = calendar.get(Calendar.DAY_OF_MONTH)
                    month = calendar.get(Calendar.MONTH)
                    year = calendar.get(Calendar.YEAR)
                    val datePickerDialog = DatePickerDialog(
                        this@MainActivity,
                        this@MainActivity, year, month, day
                    )
                    datePickerDialog.show()
                } catch (exc: Exception) {
                    Toast.makeText(applicationContext,
                        Constants.ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            buySellDialogView.findViewById<Button>(R.id.btnDialogBuySellAdd).setOnClickListener {
                try {
                    buySellAlertDialog.dismiss()

                    val pieces =
                        buySellDialogView.findViewById<EditText>(R.id.etPieces).text.toString()
                    val value =
                        buySellDialogView.findViewById<EditText>(R.id.etValue).text.toString()
                    val totalCost = pieces.toBigDecimal().multiply(value.toBigDecimal())
                    val dateTime = buySellDialogView
                        .findViewById<TextView>(R.id.tvDialogBuySellDateTime).text.toString()

                    GlobalScope.launch {
                        kotlin.runCatching {
                            val buySell = BuySell()
                            buySell.pieces = pieces.toLong()
                            buySell.value = value
                            buySell.totalCost = totalCost.toString()
                            buySell.dateTime = dateTime
                            buySell.fkSymbolID = DataProvider.getSymbolId(selectedBuySellSymbol)

                            val insertedID = DataProvider.addBuySell(buySell)

                            buySell.recordID = insertedID
                            buySellListAdapter.buySellList.add(buySell)

                            val newTotalPieces =
                                DataProvider.getTotalPieces(selectedBuySellSymbol).toString()
                            val newTotalCost = DataProvider.getTotalCost(selectedBuySellSymbol)

                            withContext(Dispatchers.Main) {
                                try {
                                    buySellListAdapter.notifyDataSetChanged()

                                    findViewById<TextView>(R.id.tvTotalPiecesNumber).text =
                                        newTotalPieces
                                    findViewById<TextView>(R.id.tvTotalCostValue).text =
                                        newTotalCost
                                } catch (exc: Exception) {
                                    Toast.makeText(applicationContext,
                                        Constants.ERROR_MESSAGE,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }.onFailure {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext,
                                    Constants.ERROR_MESSAGE,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } catch (exc: Exception) {
                    Toast.makeText(applicationContext,
                        Constants.ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            buySellDialogView.findViewById<Button>(R.id.btnDialogBuySellCancel).setOnClickListener {
                try {
                    buySellAlertDialog.dismiss()
                } catch (exc: Exception) {
                    Toast.makeText(applicationContext,
                        Constants.ERROR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendToDriveDialog() {
        requestSignIn()

        try {
            val sendDialogView = LayoutInflater.from(applicationContext)
                .inflate(R.layout.dialog_upload_to_drive, null, false)
            val sendDialogBuilder =
                AlertDialog.Builder(this).setView(sendDialogView).setTitle(R.string.sync_with_drive)
            val sendAlertDialog = sendDialogBuilder.show()

            sendDialogView.findViewById<Button>(R.id.btnDialogUpload).setOnClickListener {
                sendAlertDialog.dismiss()
                setProgressDialog()
                var dbFileIdInDrive = Constants.EMPTY_STRING

                driveServiceHelper.queryFiles()?.addOnSuccessListener {
                    try {
                        val filesList = it?.files
                        if (filesList != null) {
                            for (file in filesList) {
                                if (file.name == Constants.DATABASE_NAME_WITH_EXTENSION) {
                                    dbFileIdInDrive = file.id
                                }
                            }

                            if (dbFileIdInDrive == Constants.EMPTY_STRING) {
                                dialogLoading.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Dosya bulunamadı!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                driveServiceHelper.updateDBFile(dbFileIdInDrive)
                                    ?.addOnSuccessListener {
                                        dialogLoading.dismiss()
                                        Toast.makeText(
                                            applicationContext,
                                            "Yükleme Başarılı",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }?.addOnFailureListener {
                                    dialogLoading.dismiss()
                                    Toast.makeText(
                                        applicationContext,
                                        "Yüklenemedi!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } catch (exc: Exception) {
                        Toast.makeText(applicationContext,
                            Constants.ERROR_MESSAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }?.addOnFailureListener {
                    try {
                        dialogLoading.dismiss()
                        Toast.makeText(applicationContext,
                            "Dosya bulunamadı!",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (exc: Exception) {
                        Toast.makeText(applicationContext,
                            Constants.ERROR_MESSAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestSignIn() {
        try {
            val signInOptions: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                    .build()

            client = GoogleSignIn.getClient(this, signInOptions)

            startActivityForResult(client.signInIntent, Constants.REQUEST_CODE_SIGN_IN)
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setProgressDialog() {
        try {
            val llPadding = 30
            val ll = LinearLayout(this)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.setPadding(llPadding, llPadding, llPadding, llPadding)
            ll.gravity = Gravity.CENTER
            var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            ll.layoutParams = llParam
            val progressBar = ProgressBar(this)
            progressBar.isIndeterminate = true
            progressBar.setPadding(0, 0, llPadding, 0)
            progressBar.layoutParams = llParam
            llParam = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            val tvText = TextView(this)
            tvText.text = "Yükleniyor ..."
            tvText.setTextColor(Color.parseColor("#000000"))
            tvText.textSize = 20f
            tvText.layoutParams = llParam
            ll.addView(progressBar)
            ll.addView(tvText)
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setView(ll)
            dialogLoading = builder.create()
            dialogLoading.show()
            val window = dialogLoading.window
            if (window != null) {
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialogLoading.window!!.attributes)
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                dialogLoading.window!!.attributes = layoutParams
            }
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showTotalInvestmentDialog() {
        GlobalScope.launch {
            kotlin.runCatching {
                val message = DataProvider.getTotalInvestment()

                withContext(Dispatchers.Main) {
                    try {
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle(R.string.capital)
                        alertDialog.setMessage(message)
                        alertDialog.setCancelable(true)
                        alertDialog.setNeutralButton(R.string.ok_text) { _, _ -> }
                        alertDialog.show()
                    } catch (exc: Exception) {
                        Toast.makeText(applicationContext,
                            Constants.ERROR_MESSAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.onFailure {
                Toast.makeText(applicationContext,
                    Constants.ERROR_MESSAGE,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        selectedBuySellSymbol = Constants.EMPTY_STRING

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
        try {
            myDay = dayOfMonth
            myYear = year
            myMonth = month + 1
            val calendar: Calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR)
            minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this@MainActivity,
                this@MainActivity, hour, minute, DateFormat.is24HourFormat(this)
            )
            timePickerDialog.show()
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        try {
            myHour = hourOfDay
            myMinute = minute
            val txt = myYear.toString() + "-" +
                    myMonth.toString().padStart(2, '0') + "-" +
                    myDay.toString().padStart(2, '0') + " " +
                    myHour.toString().padStart(2, '0') + ":" +
                    myMinute.toString().padStart(2, '0')
            buySellDialogView.findViewById<TextView>(R.id.tvDialogBuySellDateTime).text = txt
        } catch (exc: Exception) {
            Toast.makeText(applicationContext,
                Constants.ERROR_MESSAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setSelectedBuySellSymbol(selectedSymbol: String) {
        selectedBuySellSymbol = selectedSymbol
    }
}