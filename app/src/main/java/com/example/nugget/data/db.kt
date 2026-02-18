package com.example.nugget.data

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

// Define your data class for a food item
data class FoodItem(
    val foodName: String? = null,
    val dateBought: String? = null,
    val expiryDate: String? = null
)
suspend fun getFoodItems(): List<FoodItem> {
    val database = Firebase.database("https://mad-food-storage-default-rtdb.europe-west1.firebasedatabase.app/")
    val snapshot = database.getReference("foodItems").get().await()

    return snapshot.children.mapNotNull {
        it.getValue(FoodItem::class.java)
    }
}
