package com.example.mad_project.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val BASE_URL = "https://world.openfoodfacts.net/api/v2/product"
private const val FIELDS = "product_name,brands,generic_name"

/**
 * Sources: https://world.openfoodfacts.net/api/v2/product,
 * Open Food Facts API (fields: code, status, product_name, brands, image_url, generic_name).
 *
 * Fetches product by barcode via GET request; uses HttpURLConnection and JSONObject.
 * Runs on Dispatchers.IO. Returns null if product not found or on network/parse error.
 *
 * Prompt: Use Open Food Facts API to identify what the items the barcodes belong to.
 */
suspend fun fetchProductByBarcode(barcode: String): OpenFoodFactsProduct? = withContext(Dispatchers.IO) {
    try {
        val url = URL("$BASE_URL/$barcode.json?fields=code,status,$FIELDS")
        val conn = url.openConnection() as? HttpURLConnection ?: return@withContext null
        conn.requestMethod = "GET"
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = conn.inputStream.bufferedReader().readText()
            val root = JSONObject(body)
            if (root.optInt("status", 0) != 1) return@withContext null
            val product = root.optJSONObject("product") ?: return@withContext null
            OpenFoodFactsProduct(
                barcode = root.optString("code", barcode),
                productName = product.optString("product_name").takeIf { it.isNotBlank() },
                brands = product.optString("brands").takeIf { it.isNotBlank() },
                genericName = product.optString("generic_name").takeIf { it.isNotBlank() }
            )
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}
