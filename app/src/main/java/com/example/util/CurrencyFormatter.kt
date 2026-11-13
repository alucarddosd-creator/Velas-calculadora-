package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val clpFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatCLP(amount: Double): String {
        return try {
            val rounded = Math.round(amount).toDouble()
            clpFormat.format(rounded)
        } catch (e: Exception) {
            "$${amount.toInt()}"
        }
    }

    fun formatPercent(percent: Double): String {
        return String.format(Locale("es", "CL"), "%.1f%%", percent)
    }

    fun formatNumber(value: Double, decimals: Int = 1): String {
        return String.format(Locale("es", "CL"), "%.${decimals}f", value)
    }
}
