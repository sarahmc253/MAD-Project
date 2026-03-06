package com.example.mad_project.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

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
    Log.d(TAG, "setValueFlow: $value ${FirebaseWriteResult.Loading}")
    emit(FirebaseWriteResult.Loading)

    try {
        setValue(value).await()
        Log.d(TAG, "setValueFlow: $value ${FirebaseWriteResult.Success}")
        emit(FirebaseWriteResult.Success)
    } catch (e: Exception) {
        emit(FirebaseWriteResult.Error(e.message ?: "Write failed", e))
    }
}

fun DatabaseReference.removeValueFlow(): Flow<FirebaseWriteResult> = flow {
    emit(FirebaseWriteResult.Loading)

    try {
        removeValue().await()
        Log.d(TAG, "removeValueFlow: ${FirebaseWriteResult.Success}")
        emit(FirebaseWriteResult.Success)
    } catch (e: Exception) {
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