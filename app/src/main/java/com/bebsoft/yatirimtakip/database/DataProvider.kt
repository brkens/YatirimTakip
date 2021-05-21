package com.bebsoft.yatirimtakip.database

import java.math.BigDecimal
import java.math.MathContext

class DataProvider {

    companion object {

        private lateinit var investDatabase : InvestDatabase

        fun setDatabase(db: InvestDatabase) {
            investDatabase = db
        }

        suspend fun getSymbolId(symbolName: String) : Long {
            return investDatabase.symbolDao.getSymbolID(symbolName)
        }

        private suspend fun getMeanValue(symbolName: String): String {
            val symbolID = investDatabase.symbolDao.getSymbolID(symbolName)
            val buySellRecordsList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var sumOfTotalCosts = BigDecimal(0.0)
            var sumOfPieces = 0

            for (record in buySellRecordsList) {
                sumOfTotalCosts = sumOfTotalCosts.add(record.totalCost.toBigDecimal())
                sumOfPieces += record.pieces.toInt()
            }

            var meanValue = "NaN"
            if (sumOfPieces != 0) {
                meanValue = String.format("%.2f", (sumOfTotalCosts.divide(sumOfPieces.toBigDecimal(), MathContext.DECIMAL32)))
            }

            return meanValue
        }

        suspend fun deleteSymbol(symbolName: String) {
            investDatabase.symbolDao.deleteSymbolCascaded(symbolName)
        }

        suspend fun getSymbolList(): List<Symbol> {
            return investDatabase.symbolDao.getSymbols()
        }

        suspend fun getBuySellRecords(currentSymbol: String): List<BuySell> {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            return investDatabase.buySellDao.getBuySellRecords(symbolID)
        }

        suspend fun deleteBuySellRecord(recordID: Long) {
            investDatabase.buySellDao.deleteBuySellRecord(recordID)
        }

        suspend fun getTotalPieces(currentSymbol: String): Int {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var totalPieces = 0

            for (buySell in currentSymbolBuySellList) {
                totalPieces += buySell.pieces.toInt()
            }

            return totalPieces
        }

        suspend fun getTotalCost(currentSymbol: String): String {
            val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
            val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

            var totalCost = BigDecimal(0.0)

            for (buySell in currentSymbolBuySellList) {
                totalCost = totalCost.add(buySell.totalCost.toBigDecimal())
            }

            return String.format("%.2f", totalCost)
        }

        suspend fun getTotalInvestment(): String {
            return String.format("%.2f", investDatabase.buySellDao.getTotalInvestment())
        }

        suspend fun addSymbol(symbolName: String) : Long {
            val symbol = Symbol()
            symbol.symbolName = symbolName
            return investDatabase.symbolDao.insert(symbol)
        }

        suspend fun addBuySell(buySell: BuySell): Long {
            return investDatabase.buySellDao.insert(buySell)
        }

        suspend fun symbolExists(symbolName: String) : Boolean {
            val count = investDatabase.symbolDao.getSymbolCount(symbolName)
            return count != 0
        }

        suspend fun getAllMeanValuesMap(): HashMap<String, String> {
            val map : HashMap<String, String> = HashMap()
            val list = getSymbolList()
            for (ele in list) {
                map[ele.symbolName] = getMeanValue(ele.symbolName)
            }
            return map
        }

        suspend fun getAllTotalPiecesMap(): HashMap<String, Int> {
            val map : HashMap<String, Int> = HashMap()
            val list = getSymbolList()
            for (ele in list) {
                map[ele.symbolName] = getTotalPieces(ele.symbolName)
            }
            return map
        }
    }
}