package com.example.lab5_alarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class
Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // "1,2,3,4,5" for Mon-Fri
    val timeInMillis: Long = 0L
) {
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}