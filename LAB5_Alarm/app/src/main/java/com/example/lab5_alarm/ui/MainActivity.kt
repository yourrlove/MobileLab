package com.example.lab5_alarm.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab5_alarm.data.model.Alarm
import com.example.lab5_alarm.databinding.ActivityMainBinding
import com.example.lab5_alarm.domain.AlarmScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alarmScheduler: AlarmScheduler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { !it }) {
            Toast.makeText(this, "Permissions are required for the app to function.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        }

        setupPermissions()
        setupViewModel()
        setupRecyclerView()
        setupFab()
    }

    private fun setupPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun setupViewModel() {
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        alarmScheduler = AlarmScheduler(this)
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggleAlarm = { alarm ->
                val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
                alarmViewModel.update(updatedAlarm)

                if (updatedAlarm.isEnabled) {
                    alarmScheduler.scheduleRepeatingAlarm(updatedAlarm)
                } else {
                    alarmScheduler.cancelAlarm(updatedAlarm.id)
                }
            },
            onDeleteAlarm = { alarm -> showDeleteConfirmation(alarm) }
        )

        binding.recyclerViewAlarms.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
            }
        }

        alarmViewModel.allAlarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms) {
                binding.recyclerViewAlarms.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
            }

            binding.textViewNoAlarms.visibility = if (alarms.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewAlarms.visibility = if (alarms.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupFab() {
        binding.fabAddAlarm.setOnClickListener { showAddAlarmDialog() }
    }

    private fun showAddAlarmDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            showLabelAndRepeatDialog(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun showLabelAndRepeatDialog(hour: Int, minute: Int) {
        val dialogView = layoutInflater.inflate(com.example.lab5_alarm.R.layout.dialog_alarm_label, null)
        val editTextLabel = dialogView.findViewById<TextInputEditText>(com.example.lab5_alarm.R.id.editTextLabel)
        val repeatButtons = listOf<MaterialButton>(
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonSun),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonMon),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonTue),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonWed),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonThu),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonFri),
            dialogView.findViewById(com.example.lab5_alarm.R.id.buttonSat)
        )

        val selectedDays = BooleanArray(7) { false }

        val primaryColor = ContextCompat.getColorStateList(this, com.example.lab5_alarm.R.color.primary)
        val textPrimaryColor = ContextCompat.getColorStateList(this, com.example.lab5_alarm.R.color.text_primary)
        val transparentColor = ContextCompat.getColorStateList(this, android.R.color.transparent)

        repeatButtons.forEachIndexed { index, button ->
            updateDayButtonUI(button, selectedDays[index], primaryColor, textPrimaryColor, transparentColor)

            button.setOnClickListener {
                selectedDays[index] = !selectedDays[index]
                updateDayButtonUI(button, selectedDays[index], primaryColor, textPrimaryColor, transparentColor)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Alarm")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val label = editTextLabel.text.toString().ifEmpty { "Alarm" }
                val repeatDays = selectedDays.mapIndexed { index, selected ->
                    if (selected) index.toString() else null
                }.joinToString(",")

                createAlarm(hour, minute, label, repeatDays)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateDayButtonUI(
        button: MaterialButton,
        isSelected: Boolean,
        primaryColor: android.content.res.ColorStateList?,
        textColor: android.content.res.ColorStateList?,
        transparent: android.content.res.ColorStateList?
    ) {
        if (isSelected) {
            button.backgroundTintList = primaryColor
            button.setTextColor(ContextCompat.getColorStateList(this, com.example.lab5_alarm.R.color.on_primary))
        } else {
            button.backgroundTintList = transparent
            button.setTextColor(textColor)
        }
    }

    private fun createAlarm(hour: Int, minute: Int, label: String, repeatDays: String) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarm = Alarm(
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            repeatDays = repeatDays,
            timeInMillis = calendar.timeInMillis
        )

        alarmViewModel.insert(alarm) { alarmId ->
            val alarmWithId = alarm.copy(id = alarmId.toInt())
            alarmScheduler.scheduleRepeatingAlarm(alarmWithId)

            runOnUiThread {
                Toast.makeText(this, "Alarm set at ${alarm.getTimeString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation(alarm: Alarm) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete this alarm?")
            .setPositiveButton("Delete") { _, _ ->
                alarmViewModel.delete(alarm)
                alarmScheduler.cancelAlarm(alarm.id)
                Toast.makeText(this, "Alarm deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
