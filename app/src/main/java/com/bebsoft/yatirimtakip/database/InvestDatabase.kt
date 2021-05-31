package com.bebsoft.yatirimtakip.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bebsoft.yatirimtakip.Constants

@Database(entities = [Symbol::class, BuySell::class], version = 1, exportSchema = false)
abstract class InvestDatabase : RoomDatabase() {

    abstract val symbolDao: SymbolDao
    abstract val buySellDao: BuySellDao

    companion object {
        @Volatile
        private var INSTANCE: InvestDatabase? = null

        fun getInstance(context: Context): InvestDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        InvestDatabase::class.java,
                        Constants.DATABASE_NAME_WITH_EXTENSION
                    ).fallbackToDestructiveMigration()
                        .setJournalMode(JournalMode.TRUNCATE)
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }

}