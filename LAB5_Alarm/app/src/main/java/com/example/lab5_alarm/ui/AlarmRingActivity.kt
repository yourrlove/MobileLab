package com.example.lab5_alarm.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lab5_alarm.data.local.AlarmDatabase
import com.example.lab5_alarm.databinding.ActivityAlarmRingBinding
import com.example.lab5_alarm.domain.AlarmScheduler
import com.example.lab5_alarm.domain.AlarmService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AlarmRingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingBinding
    private lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmScheduler = AlarmScheduler(this)

        val alarmId = intent.getIntExtra("alarm_id", -1)
        val alarmLabel = intent.getStringExtra("alarm_label") ?: "Alarm"

        if (alarmId != -1) {
            getAlarmTime(alarmId)
            binding.textViewAlarmLabel.text = alarmLabel
        } else {
            binding.textViewAlarmTime.text = "--:--"
            binding.textViewAlarmLabel.text = "Unknown Alarm"
        }

        binding.buttonDismiss.setOnClickListener {
            dismissAlarm(alarmId)
            stopService(Intent(this, AlarmService::class.java))
            finish()
        }

        // Keep screen on while alarm is ringing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getAlarmTime(alarmId: Int): String {
        lifecycleScope.launch {
            val alarm = AlarmDatabase.getDatabase(this@AlarmRingActivity)
                .alarmDao()
                .getAlarmById(alarmId)

            if (alarm != null) {
                val formattedTime = alarm.getTimeString()
                binding.textViewAlarmTime.text = formattedTime
            } else {
                binding.textViewAlarmTime.text = "--:--"
                Toast.makeText(this@AlarmRingActivity, "Alarm not found", Toast.LENGTH_SHORT).show()
            }
        }
        return ""
    }

    private fun dismissAlarm(alarmId: Int) {
        if (alarmId != -1) {
            alarmScheduler.cancelAlarm(alarmId)
            Toast.makeText(this, "Alarm dismissed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error: Alarm not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
