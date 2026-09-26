package com.pengurur.jarakradius.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pengurur.jarakradius.domain.model.MarkerPoint

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusCm: Double,
    val isFavorite: Boolean,
    val createdAt: Long
) {
    fun toDomain() = MarkerPoint(id, name, latitude, longitude, radiusCm, isFavorite, createdAt)

    companion object {
        fun fromDomain(m: MarkerPoint) =
            MarkerEntity(m.id, m.name, m.latitude, m.longitude, m.radiusCm, m.isFavorite, m.createdAt)
    }
}
