package com.example.mad_project.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

val DB_URL = "https://mad-food-storage-default-rtdb.europe-west1.firebasedatabase.app/"
val PANTRY_REF_PATH = "foodItems"

data class PantryModel(
    val foodName: String? = null,
    val dateScanned: String? = null,
    val expiryDate: String? = null,
    val quantity: Float? = null
)

data class FoodItem(
    val foodName: String? = null,
    val dateBought: String? = null,
    val expiryDate: String? = null
)

//Gets a list of items from a database reference
fun <T> DatabaseReference.addValueEventListenerFlow(
    logName: String = "DatabaseReference",
    mapper: (DataSnapshot) -> T
): Flow<T> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d(TAG, "$logName:onDataChange: ${dataSnapshot.key}")
            trySend(mapper(dataSnapshot))
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(TAG, "$logName:onCancelled", error.toException())
            close(error.toException())
        }
    }

    addValueEventListener(listener)

    awaitClose {
        removeEventListener(listener)
    }
}

//Gets an item from a database reference
fun <T> DatabaseReference.addValueEventListenerFlow(dataType: Class<T>): Flow<T?> {
    return addValueEventListenerFlow(
        logName = dataType.simpleName ?: "UnknownType"
    ) { dataSnapshot ->
        dataSnapshot.getValue(dataType)
    }
}

fun getPantry(): Flow<List<FoodItem>> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH)

    return pantryRef.addValueEventListenerFlow(
        logName = "getPantry"
    ) { dataSnapshot ->
        dataSnapshot.children.mapNotNull { child ->
            child.getValue(FoodItem::class.java)
        }
    }
}