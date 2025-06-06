package com.example.lab5_asynctask
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var textCount: TextView

    private var boundService: BoundService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as BoundService.LocalBinder
            boundService = localBinder.getService()
            isBound = true
            Toast.makeText(this@MainActivity, "Bound to service", Toast.LENGTH_SHORT).show()
//            lifecycleScope.launch {
//                while (isBound) {
//                    val count = boundService?.getCurrentCount() ?: 0
//                    textCount.text = "Count: $count"
//                    delay(1000)
//                }
//            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        checkAndPromptNotificationPermission(this)

        // Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channelId",
                "Foreground Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Background Task
        findViewById<Button>(R.id.btnBackgroundTask).setOnClickListener {
            startBackgroundTask(this)
        }

        // Foreground Service
        findViewById<Button>(R.id.btnForegroundService).setOnClickListener {
            val intent = Intent(this, ForegroundService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }

        // Bound Service
        findViewById<Button>(R.id.btnBindService).setOnClickListener {
            if (isBound) {
                unbindService(serviceConnection)
                isBound = false
            }
            val intent = Intent(this, BoundService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }


    // Check the notification is on or off
    private fun checkAndPromptNotificationPermission(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!manager.areNotificationsEnabled()) {
            Toast.makeText(context, "App notification is turned off", Toast.LENGTH_LONG).show()

            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun startBackgroundTask(context: Context) {
        // Launch coroutine from a lifecycle-aware scope (e.g., in Activity)
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..10) {
                delay(1000) // Non-blocking delay
                Log.d("BackgroundTask", "Count: $i")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Background task finished", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
