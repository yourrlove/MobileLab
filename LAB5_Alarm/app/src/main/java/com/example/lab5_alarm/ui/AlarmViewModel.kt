package com.example.lab5_alarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.lab5_alarm.data.local.AlarmDatabase
import com.example.lab5_alarm.data.model.Alarm
import com.example.lab5_alarm.data.repository.AlarmRepository
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository
    val allAlarms: LiveData<List<Alarm>>

    init {
        val alarmDao = AlarmDatabase.Companion.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
    }

    fun insert(alarm: Alarm, callback: (Long) -> Unit) = viewModelScope.launch {
        val id = repository.insert(alarm)
        callback(id)
    }

    fun update(alarm: Alarm) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun delete(alarm: Alarm) = viewModelScope.launch {
        repository.delete(alarm)
    }
}