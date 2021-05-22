package com.bebsoft.yatirimtakip.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.R

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
    var value : String = Constants.emptyString,

    @ColumnInfo(name = "total_cost")
    var totalCost : String = Constants.emptyString,

    @ColumnInfo(name = "date_time")
    var dateTime : String = Constants.emptyString,

    @ColumnInfo(name = "fk_symbol_id", index = true)
    var fkSymbolID : Long = 0L
)