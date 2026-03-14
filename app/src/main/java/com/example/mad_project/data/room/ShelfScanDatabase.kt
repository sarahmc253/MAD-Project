package com.example.mad_project.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AI-generated: Room database singleton.
 * Version starts at 1; increment and add a Migration whenever the schema changes.
 * exportSchema is false to keep the project simple — add schema exports and migration
 * tests if the schema stabilises and the app goes to production.
 *
 * Prompt: "Create a Room RoomDatabase singleton class for an Android app with one entity."
 */
@Database(entities = [PantryItemEntity::class], version = 1, exportSchema = false)
abstract class ShelfScanDatabase : RoomDatabase() {

    abstract fun pantryItemDao(): PantryItemDao

    companion object {
        @Volatile
        private var INSTANCE: ShelfScanDatabase? = null

        fun getInstance(context: Context): ShelfScanDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShelfScanDatabase::class.java,
                    "nugget_db"
                ).build().also { INSTANCE = it }
            }
    }
}
