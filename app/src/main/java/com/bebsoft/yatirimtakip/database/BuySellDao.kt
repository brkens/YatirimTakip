package com.bebsoft.yatirimtakip.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BuySellDao {

    @Insert
    suspend fun insert(record: BuySell) : Long

    @Query("SELECT * FROM buy_sell_table WHERE fk_symbol_id = :symbolID ORDER BY date_time DESC")
    suspend fun getBuySellRecords(symbolID : Long) : List<BuySell>

    @Query("DELETE FROM buy_sell_table WHERE record_id = :recordID")
    suspend fun deleteBuySellRecord(recordID: Long)

    @Query("SELECT SUM(total_cost) FROM buy_sell_table")
    suspend fun getTotalInvestment() : Double
}