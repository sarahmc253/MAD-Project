package com.example.mad_project.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * AI-generated: Emits the current internet connectivity state and updates whenever it changes.
 * Uses ConnectivityManager.NetworkCallback wrapped in a callbackFlow so it is lifecycle-safe
 * and cancellable. distinctUntilChanged suppresses duplicate emissions.
 *
 * Requires ACCESS_NETWORK_STATE permission in AndroidManifest.xml.
 *
 * Prompt: "Write a Kotlin Flow that emits a Boolean for network connectivity changes using
 * ConnectivityManager NetworkCallback and callbackFlow."
 */
fun Context.networkConnectivityFlow(): Flow<Boolean> = callbackFlow {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }
        override fun onLost(network: Network) {
            trySend(false)
        }
    }

    val active = connectivityManager.activeNetwork
    val caps = connectivityManager.getNetworkCapabilities(active)
    trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    connectivityManager.registerNetworkCallback(request, callback)

    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
