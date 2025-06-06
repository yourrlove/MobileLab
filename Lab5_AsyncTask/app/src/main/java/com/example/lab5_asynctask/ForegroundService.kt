package com.example.lab5_asynctask

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Foreground Service")
            .setContentText("Running task...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..10) {
                delay(1000)
                Log.d("ForegroundService", "Count: $i")
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}