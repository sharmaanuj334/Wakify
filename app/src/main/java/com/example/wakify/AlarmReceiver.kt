package com.example.wakify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null

        fun acquireWakeLock(context: Context) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "wakify:alarmReceiverWakeLock"
            ).apply { acquire(60 * 1000L) } // Hold for 60s max to bridge to service
        }

        fun releaseWakeLock() {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val requestCode = intent?.getIntExtra("requestCode", -1) ?: -1
        Log.d("AlarmReceiver", "Alarm triggered with request code: $requestCode")

        // Acquire a WakeLock immediately to prevent CPU from sleeping
        // before the foreground service starts and acquires its own WakeLock
        acquireWakeLock(context)

        // Reschedule this alarm for the next day so it repeats daily
        val alarms = AlarmStorage.loadAlarms(context)
        val alarm = alarms.find { it.requestCode == requestCode }
        if (alarm != null) {
            AlarmScheduler.scheduleAlarm(context, alarm)
            Log.d("AlarmReceiver", "Rescheduled alarm for next day: ${alarm.hour}:${alarm.minute}")
        }

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("requestCode", requestCode)
        }
        context.startForegroundService(serviceIntent)
    }
}
