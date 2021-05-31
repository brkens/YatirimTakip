package com.bebsoft.yatirimtakip.database

import com.bebsoft.yatirimtakip.Constants
import java.lang.Exception
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.collections.HashMap

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
            var meanValue = Constants.EMPTY_STRING

            try {
                val symbolID = investDatabase.symbolDao.getSymbolID(symbolName)
                val buySellRecordsList = investDatabase.buySellDao.getBuySellRecords(symbolID)

                var sumOfTotalCosts = BigDecimal(0.0)
                var sumOfPieces = 0

                for (record in buySellRecordsList) {
                    sumOfTotalCosts = sumOfTotalCosts.add(record.totalCost.toBigDecimal())
                    sumOfPieces += record.pieces.toInt()
                }

                if (sumOfPieces != 0) {
                    val mean =
                        sumOfTotalCosts.divide(sumOfPieces.toBigDecimal(), MathContext.DECIMAL32)
                    meanValue = mean.setScale(2, RoundingMode.UP).toString()
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
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
            var totalPieces = 0

            try {
                val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
                val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

                for (buySell in currentSymbolBuySellList) {
                    totalPieces += buySell.pieces.toInt()
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            return totalPieces
        }

        suspend fun getTotalCost(currentSymbol: String): String {
            var totalCost = BigDecimal(0.0)

            try {
                val symbolID = investDatabase.symbolDao.getSymbolID(currentSymbol)
                val currentSymbolBuySellList = investDatabase.buySellDao.getBuySellRecords(symbolID)

                for (buySell in currentSymbolBuySellList) {
                    totalCost = totalCost.add(buySell.totalCost.toBigDecimal())
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            return totalCost.setScale(2, RoundingMode.UP).toString()
        }

        suspend fun getTotalInvestment(): String {
            var totalInv: Double = 0.0

            try {
                totalInv = investDatabase.buySellDao.getTotalInvestment()
            } catch(e: Exception) {
                return Constants.EMPTY_STRING
            }

            return totalInv.toBigDecimal().setScale(2, RoundingMode.UP).toString()
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

            try {
                val list = getSymbolList()
                for (ele in list) {
                    map[ele.symbolName] = getMeanValue(ele.symbolName)
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            return map
        }

        suspend fun getAllTotalPiecesMap(): HashMap<String, Int> {
            val map : HashMap<String, Int> = HashMap()

            try {
                val list = getSymbolList()
                for (ele in list) {
                    map[ele.symbolName] = getTotalPieces(ele.symbolName)
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            return map
        }
    }
}