package com.example.wakify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("requestCode", alarm.requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
            pendingIntent
        )

        Log.d(TAG, "Scheduled alarm ${alarm.hour}:${alarm.minute} for ${calendar.time}")
    }

    fun cancelAlarm(context: Context, alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm ${alarm.hour}:${alarm.minute}")
    }

    fun rescheduleAllAlarms(context: Context) {
        val alarms = AlarmStorage.loadAlarms(context)
        for (alarm in alarms) {
            scheduleAlarm(context, alarm)
        }
        Log.d(TAG, "Rescheduled ${alarms.size} alarms")
    }
}
