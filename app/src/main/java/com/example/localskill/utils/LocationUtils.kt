package com.example.localskill.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatDistance(distanceKm: Double): String =
        if (distanceKm < 1.0) {
            "${(distanceKm * 1000).roundToInt()} m away"
        } else if (distanceKm < 10.0) {
            "${"%.1f".format(distanceKm)} km away"
        } else {
            "${distanceKm.roundToInt()} km away"
        }

    fun distanceKmOrNull(
        fromLat: Double?,
        fromLon: Double?,
        toLat: Double?,
        toLon: Double?
    ): Double? {
        if (fromLat == null || fromLon == null || toLat == null || toLon == null) return null
        return calculateDistanceKm(fromLat, fromLon, toLat, toLon)
    }
}
