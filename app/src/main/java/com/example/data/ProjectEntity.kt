package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val videoUri: String = "",
    val durationSeconds: Int = 30,
    val resolution: String = "1080p",
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filterType: String = "Normal",
    val textOverlay: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
