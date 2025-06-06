package com.example.lab5_alarm.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lab5_alarm.domain.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = AlarmScheduler(context)
            val database = AlarmDatabase.Companion.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = database.alarmDao().getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    alarmScheduler.scheduleRepeatingAlarm(alarm)
                }
            }
        }
    }
}