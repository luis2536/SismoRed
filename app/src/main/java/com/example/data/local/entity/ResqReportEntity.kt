package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resq_reports",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["timestamp"])
    ]
)
data class ResqReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // e.g., "Sismo", "Persona_Desaparecida", "Peligro"
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // Crucial for offline-first queue
)
