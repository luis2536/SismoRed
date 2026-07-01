package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val role: String, // e.g. "Rescatista", "Civil"
    val bioHash: String? = null, // Mock for face telemetry hash
    val lastKnownLat: Double? = null,
    val lastKnownLon: Double? = null
)
