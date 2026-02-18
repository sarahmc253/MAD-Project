package com.example.nugget.data

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

// Define your data class for a food item
data class FoodItem(
    val foodName: String? = null,
    val dateBought: String? = null,
    val expiryDate: String? = null
)
fun getFoodItems() {
    val database = Firebase.database("https://mad-food-storage-default-rtdb.europe-west1.firebasedatabase.app/")
    val myRef = database.getReference("foodItems")

    myRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val foodItemsList = mutableListOf<FoodItem>()
            for (foodSnapshot in snapshot.children) {
                val foodItem = foodSnapshot.getValue(FoodItem::class.java)
                foodItem?.let { foodItemsList.add(it) }
            }
            println("Food Items: $foodItemsList")
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
            println("Failed to read value: ${error.toException()}")
        }
    })
}
