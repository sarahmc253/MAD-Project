package com.example.mad_project.data

/** Product info from Open Food Facts barcode lookup (product_name, brands, image_url, displayName). */
data class OpenFoodFactsProduct(
    val barcode: String,
    val productName: String?,
    val brands: String?,
    val imageUrl: String?,
    val genericName: String?
) {
    /** Display name: product name, or "Brand - Barcode" if no name, or just barcode */
    val displayName: String
        get() = productName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(brands, barcode).joinToString(" – ")
            ?: barcode
}
