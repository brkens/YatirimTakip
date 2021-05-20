package com.bebsoft.yatirimtakip

import com.bebsoft.yatirimtakip.database.BuySell
import com.bebsoft.yatirimtakip.database.InvestDatabase
import com.bebsoft.yatirimtakip.database.Symbol

class DataProvider {

    companion object {

        private lateinit var investDatabase : InvestDatabase

        fun setDatabase(db: InvestDatabase) {
            investDatabase=db
        }

        fun getMeanValue(symbolName: String): String {
            val symbolID = investDatabase.symbolDao.getSymbolID(symbolName)
            val buySellRecordsList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var sumOfTotalCosts = 0.0
            var sumOfPieces = 0

            for (record in buySellRecordsList) {
                sumOfTotalCosts += record.totalCost.toDouble()
                sumOfPieces += record.pieces.toInt()
            }

            return String.format("%.2f", (sumOfTotalCosts / sumOfPieces))
        }

        fun deleteSymbol(symbolName: String) {
            investDatabase.symbolDao.deleteSymbolCascaded(symbolName)
        }

        fun getSymbolList(): List<Symbol> {
            return investDatabase.symbolDao.getSymbols()
        }

        fun getBuySellRecords(currentSymbol: String): List<BuySell> {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            return investDatabase.buySellDao.getBuySellRecords(symbolID)
        }

        fun deleteBuySellRecord(recordID: Long) {
            investDatabase.buySellDao.deleteBuySellRecord(recordID)
        }

        fun getTotalPieces(currentSymbol: String): Int {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var totalPieces = 0

            for (buySell in currentSymbolBuySellList) {
                totalPieces += buySell.pieces.toInt()
            }

            return totalPieces
        }

        fun getTotalCost(currentSymbol: String): String {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var totalCost = 0.0

            for (buySell in currentSymbolBuySellList) {
                totalCost += buySell.totalCost.toDouble()
            }

            return String.format("%.2f", totalCost)
        }

        fun getTotalInvestment(): String {
            return String.format("%.2f", investDatabase.buySellDao.getTotalInvestment())
        }

        fun addSymbol(symbolName: String) : Long {
            var symbol = Symbol()
            symbol.symbolName = symbolName
            return investDatabase.symbolDao.insert(symbol)
        }
    }
}