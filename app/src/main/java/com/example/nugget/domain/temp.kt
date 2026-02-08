package com.example.nugget.domain

// This is where logic, rules and calculations live.

/*For example:
*object ExpiryCalculator {

    fun daysUntilExpiry(item: Item, today: LocalDate = LocalDate.now()): Long? {
        val expiry = item.expiryDate ?: return null
        return ChronoUnit.DAYS.between(today, expiry)
    }

    fun isExpired(item: Item, today: LocalDate = LocalDate.now()): Boolean =
        daysUntilExpiry(item, today)?.let { it < 0 } ?: false

    fun isExpiringSoon(item: Item, thresholdDays: Int, today: LocalDate = LocalDate.now()): Boolean =
        daysUntilExpiry(item, today)?.let { it in 0..thresholdDays } ?: false
}
* */