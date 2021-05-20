package com.bebsoft.yatirimtakip.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SymbolDao {

    @Insert
    fun insert(symbol: Symbol) : Long

    @Query("SELECT * FROM symbol_table ORDER BY symbol_name ASC")
    fun getSymbols() : List<Symbol>

    @Query("DELETE FROM symbol_table WHERE symbol_name = :symbolName")
    fun deleteSymbolCascaded(symbolName : String)

    @Query("SELECT symbol_id FROM symbol_table WHERE symbol_name = :symbolName")
    fun getSymbolID(symbolName : String) : Long
}