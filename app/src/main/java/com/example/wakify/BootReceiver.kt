package com.example.wakify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Boot completed, re-scheduling alarms")

        val alarms = AlarmStorage.loadAlarms(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (alarm in alarms) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
            }

            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("requestCode", alarm.requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.requestCode,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("BootReceiver", "Re-scheduled alarm ${alarm.hour}:${alarm.minute}")
        }
    }
}
