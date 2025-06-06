package com.example.lab5_alarm.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lab5_alarm.data.model.Alarm
import com.example.lab5_alarm.databinding.ItemAlarmBinding


class AlarmAdapter(
    private val onToggleAlarm: (Alarm) -> Unit,
    private val onDeleteAlarm: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Alarm>() {
            override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem == newItem
            }
        }
    }

    class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val dayMap = mapOf(
            "1" to "Sun", "2" to "Mon", "3" to "Tue",
            "4" to "Wed", "5" to "Thu", "6" to "Fri", "7" to "Sat"
        )

        fun bind(alarm: Alarm, onToggleAlarm: (Alarm) -> Unit, onDeleteAlarm: (Alarm) -> Unit) {
            binding.textViewTime.text = alarm.getTimeString()
            binding.textViewLabel.text = alarm.label
            val days = alarm.repeatDays
            val selectedDays = days.split(",")
                .filter { it != "_" && it.isNotBlank() }
                .mapNotNull { dayMap[it] }
            binding.textViewRepeatDays.text = if (selectedDays.isEmpty()) "Once" else selectedDays.joinToString(", ")
            binding.switchAlarm.isChecked = alarm.isEnabled
            binding.buttonDelete.setOnClickListener { onDeleteAlarm(alarm) }

            binding.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                val updatedAlarm = alarm.copy(isEnabled = isChecked)
                onToggleAlarm(updatedAlarm)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)
        holder.bind(alarm, onToggleAlarm, onDeleteAlarm)
    }
}