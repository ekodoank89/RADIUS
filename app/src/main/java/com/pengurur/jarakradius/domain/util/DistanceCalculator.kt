package com.pengurur.jarakradius.domain.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {

    private const val EARTH_RADIUS_METER = 6_371_000.0

    /** Jarak haversine antar dua koordinat dalam meter */
    fun distanceMeter(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METER * c
    }

    /** Jarak dalam sentimeter */
    fun distanceCm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
        distanceMeter(lat1, lon1, lat2, lon2) * 100.0
}
