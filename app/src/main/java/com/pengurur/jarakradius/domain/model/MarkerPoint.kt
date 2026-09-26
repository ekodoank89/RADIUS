package com.pengurur.jarakradius.domain.model

data class MarkerPoint(
    val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusCm: Double,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
