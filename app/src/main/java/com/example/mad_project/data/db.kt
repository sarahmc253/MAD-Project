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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

val DB_URL = "https://mad-food-storage-default-rtdb.europe-west1.firebasedatabase.app/"
val PANTRY_REF_PATH = "foodItems"

//TODO: change from foodItem to this
data class PantryModel(
    val itemId: String? = null,
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

//TODO: put in prompt for generation or change
sealed class FirebaseWriteResult {
    data object Loading : FirebaseWriteResult()
    data object Success : FirebaseWriteResult()
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : FirebaseWriteResult()
}

fun <T> DatabaseReference.setValueFlow(value: T): Flow<FirebaseWriteResult> = flow {
    emit(FirebaseWriteResult.Loading)

    try {
        //TODO: do we need this when pushing?
        setValue(value).await()
        emit(FirebaseWriteResult.Success)
    } catch (e: Exception) {
        emit(FirebaseWriteResult.Error(e.message ?: "Write failed", e))
    }
}

fun DatabaseReference.removeValueFlow(): Flow<FirebaseWriteResult> = flow {
    emit(FirebaseWriteResult.Loading)

    try {
        removeValue().await()
        Log.d(TAG, "removeValueFlow:onSuccess key=$key")
        emit(FirebaseWriteResult.Success)
    } catch (e: Exception) {
        Log.w(TAG, "removeValueFlow:onFailure key=$key", e)
        emit(FirebaseWriteResult.Error(e.message ?: "Delete failed", e))
    }
}

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

//TODO: put in prompt for generation

fun addPantryItem(pantryItem: PantryModel): Flow<FirebaseWriteResult> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH)

    return pantryRef.push().setValueFlow(pantryItem)
}

fun writePantryItem(itemId: String, pantryItem: PantryModel): Flow<FirebaseWriteResult> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH)

    return pantryRef.child(itemId).setValueFlow(pantryItem)
}


fun deletePantryItem(itemId: String): Flow<FirebaseWriteResult> {
    val db = Firebase.database(DB_URL)
    val pantryRef = db.getReference(PANTRY_REF_PATH).child(itemId)
    return pantryRef.removeValueFlow()
}