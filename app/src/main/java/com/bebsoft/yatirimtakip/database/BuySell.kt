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
    var recordID : Long = 0L,

    @ColumnInfo(name = "pieces")
    var pieces : Long = 0L,

    @ColumnInfo(name = "value")
    var value : String = "X",

    @ColumnInfo(name = "total_cost")
    var totalCost : String = "X",

    @ColumnInfo(name = "date_time")
    var dateTime : String = "X",

    @ColumnInfo(name = "fk_symbol_id")
    var fkSymbolID : Long = 0L
)