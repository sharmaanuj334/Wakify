package com.example.wakify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarms: MutableList<Alarm>
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    interface OnAlarmClickListener {
        fun onAlarmClick(position: Int)
    }

    var listener: OnAlarmClickListener? = null

    inner class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val alarmText: TextView = view.findViewById(R.id.alarmTimeText)
        val alarmAmPm: TextView = view.findViewById(R.id.alarmAmPm)
        val alarmLabel: TextView = view.findViewById(R.id.alarmLabel)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteAlarmBtn)

        init {
            deleteBtn.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener?.onAlarmClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        val displayHour = when {
            alarm.hour == 0 -> 12
            alarm.hour > 12 -> alarm.hour - 12
            else -> alarm.hour
        }
        val amPm = if (alarm.hour < 12) "AM" else "PM"

        holder.alarmText.text = String.format("%d:%02d", displayHour, alarm.minute)
        holder.alarmAmPm.text = amPm
        holder.alarmLabel.text = "Tap trash to delete"
    }

    override fun getItemCount(): Int = alarms.size

    fun removeAlarm(position: Int) {
        alarms.removeAt(position)
        notifyItemRemoved(position)
    }
}
