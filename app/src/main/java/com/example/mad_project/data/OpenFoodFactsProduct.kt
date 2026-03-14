package com.example.mad_project.data

/**
 * Sources: https://world.openfoodfacts.net/api/v2/product,
 * Open Food Facts API documentation.
 *
 * Data class for product info returned by barcode lookup. Holds product_name, brands,
 * image_url, generic_name and a computed displayName for UI (product name, or brand + barcode).
 *
 * Prompt: Use Open Food Facts API to identify what the items the barcodes belong to.
 */
data class OpenFoodFactsProduct(
    val barcode: String,
    val productName: String?,
    val brands: String?,
    val genericName: String?
) {
    /** Display name: product name, or "Brand - Barcode" if no name, or just barcode */
    val displayName: String
        get() = productName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(brands, barcode).joinToString(" – ")
            ?: barcode
}
