package com.bebsoft.yatirimtakip.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "buy_sell_table",
    foreignKeys = [
        ForeignKey(
            entity = Symbol::class,
            parentColumns = ["symbol_id"],
            childColumns = ["fk_symbol_id"],
            onDelete = ForeignKey.CASCADE
        )])
data class BuySell(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    val recordID : Long,

    @ColumnInfo(name = "pieces")
    val pieces : Long,

    @ColumnInfo(name = "value")
    val value : String,

    @ColumnInfo(name = "total_cost")
    val totalCost : String,

    @ColumnInfo(name = "date_time")
    val dateTime : String,

    @ColumnInfo(name = "fk_symbol_id")
    val fkSymbolID : Long
)