package com.example.mad_project.data

import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.Flow

val DB_URL = "https://mad-food-storage-default-rtdb.europe-west1.firebasedatabase.app/"
val PANTRY_REF_PATH = "foodItems"

data class PantryModel(
    val itemId: String? = null,
    val foodName: String? = null,
    val dateScanned: String? = null,
    val expiryDate: String? = null,
    val quantity: Float? = null,
    val location: String? = null,
    val notes: String? = null,
    val category: String? = null,
    val imageUrl: String? = null
)

fun getPantry(): Flow<List<PantryModel>> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH)

    return pantryRef.addValueEventListenerFlow(
        logName = "getPantry"
    ) { dataSnapshot ->
        dataSnapshot.children.mapNotNull { child ->
            child.getValue(PantryModel::class.java)?.copy(itemId = child.key ?: "")
        }
    }
}

/**
 * I generated our solution to deal with writing and removing items in a non-relational database.
 *
 * Prompt: The attached files are code to display a list of pantry items. To do: extend the functions
 * addPantryItem, writePantryItem and deletePantryItem so that they delete the correct item in
 * a realtime firebase database.
 *
 * The rest I did myself using examples from: https://firebase.google.com/docs/database/web/read-and-write
 * and: https://canopas.com/use-firestore-and-firebase-realtime-database-with-kotlin-flow-76a8f260e31a
 */

fun writePantryItem(itemId: String, pantryItem: PantryModel): Flow<FirebaseWriteResult> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH)

    return pantryRef.child(itemId).setValueFlow(
        pantryItem.copy(itemId = itemId)
    )
}

fun deletePantryItem(itemId: String): Flow<FirebaseWriteResult> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH).child(itemId)
    return pantryRef.removeValueFlow()
}