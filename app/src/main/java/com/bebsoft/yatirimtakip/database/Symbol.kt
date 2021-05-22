package com.bebsoft.yatirimtakip.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bebsoft.yatirimtakip.Constants
import com.bebsoft.yatirimtakip.R

@Entity(tableName = "symbol_table")
data class Symbol(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "symbol_id")
    var symbolID : Long = 0L,

    @ColumnInfo(name = "symbol_name")
    var symbolName : String = Constants.emptyString
)