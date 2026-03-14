package com.example.mad_project

import android.app.Application
import com.example.mad_project.data.PantryRepository
import com.example.mad_project.data.room.ShelfScanDatabase

class ShelfScanApplication : Application() {
    val repository: PantryRepository by lazy {
        PantryRepository(ShelfScanDatabase.getInstance(this).pantryItemDao())
    }
}
