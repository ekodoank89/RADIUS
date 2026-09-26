package com.pengurur.jarakradius.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatUtil {

    private val symbols = DecimalFormatSymbols(Locale("in", "ID"))
    private val numberFormat = DecimalFormat("#,##0.##", symbols)

    fun formatCm(cm: Double): String = "${numberFormat.format(cm)} cm"

    fun formatReadable(cm: Double): String = when {
        cm >= 100_000.0 -> "${numberFormat.format(cm / 100_000.0)} km"
        else -> "${numberFormat.format(cm / 100.0)} m"
    }

    fun formatCoordinate(value: Double): String =
        String.format(Locale.US, "%.6f", value)
}
