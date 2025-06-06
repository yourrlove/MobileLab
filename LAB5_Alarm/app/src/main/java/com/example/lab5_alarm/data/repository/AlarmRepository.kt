package com.example.lab5_alarm.data.repository

import androidx.lifecycle.LiveData
import com.example.lab5_alarm.data.local.AlarmDao
import com.example.lab5_alarm.data.model.Alarm

class AlarmRepository(private val alarmDao: AlarmDao) {

    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun update(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun delete(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms()
    }
}