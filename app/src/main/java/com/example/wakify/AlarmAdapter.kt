package com.example.wakify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarms: MutableList<Alarm>
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // Interface for click listener to notify MainActivity
    interface OnAlarmClickListener {
        fun onAlarmClick(position: Int)
    }

    var listener: OnAlarmClickListener? = null

    inner class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val alarmText: TextView = view.findViewById(R.id.alarmTimeText)

        init {
            view.setOnClickListener {
                listener?.onAlarmClick(adapterPosition)
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
        holder.alarmText.text = String.format("Alarm set for %02d:%02d", alarm.hour, alarm.minute)
    }

    override fun getItemCount(): Int = alarms.size

    fun removeAlarm(position: Int) {
        alarms.removeAt(position)
        notifyItemRemoved(position)
    }
}
