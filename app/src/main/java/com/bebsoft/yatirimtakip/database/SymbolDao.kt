package com.bebsoft.yatirimtakip.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SymbolDao {

    @Insert
    suspend fun insert(symbol: Symbol) : Long

    @Query("SELECT * FROM symbol_table ORDER BY symbol_name ASC")
    suspend fun getSymbols() : List<Symbol>

    @Query("DELETE FROM symbol_table WHERE symbol_name = :symbolName")
    suspend fun deleteSymbolCascaded(symbolName : String)

    @Query("SELECT symbol_id FROM symbol_table WHERE symbol_name = :symbolName")
    suspend fun getSymbolID(symbolName : String) : Long

    @Query("SELECT COUNT(symbol_id) FROM symbol_table WHERE symbol_name = :symbolName")
    suspend fun getSymbolCount(symbolName: String) : Int
}