package com.example.lab5_alarm.domain

import android.app.*
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lab5_alarm.R
import com.example.lab5_alarm.ui.AlarmRingActivity

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private val stopRingtoneHandler = Handler(Looper.getMainLooper())
    private val stopRingtoneRunnable = Runnable {
        stopRingtone()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1
        val isSnooze = intent?.getBooleanExtra("is_snooze", false) ?: false
        val label = intent?.getStringExtra("alarm_label") ?: "Alarm"

        if (alarmId != -1) {
            // Start the foreground notification for Android 8+
            startForeground(alarmId, createNotification(alarmId, label))

            // Play ringtone
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()

            // Stop ringtone after 30 seconds
            stopRingtoneHandler.postDelayed(stopRingtoneRunnable, 30_000)

            // Launch ringing activity
            val activityIntent = Intent(this, AlarmRingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", label)
                putExtra("is_snooze", isSnooze)
            }
            startActivity(activityIntent)

            // Show notification if not snoozed
            if (!isSnooze) {
                showNotification(alarmId, label)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(alarmId: Int, label: String): Notification {
        val channelId = "alarm_service_channel"

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for active alarm notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Dismiss action (sends broadcast to cancel alarm)
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("alarm_id", alarmId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm) // Replace with your custom icon
            .setContentTitle("Alarm")
            .setContentText("Alarm \"$label\" is ringing")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(R.drawable.ic_delete, "Dismiss", dismissPendingIntent)
            .build()
    }

    private fun showNotification(alarmId: Int, label: String) {
        val channelId = "alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Triggered",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for triggered alarm notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Alarm Triggered")
            .setContentText("Alarm \"$label\" has been triggered")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(alarmId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtoneHandler.removeCallbacks(stopRingtoneRunnable)
        stopRingtone()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}