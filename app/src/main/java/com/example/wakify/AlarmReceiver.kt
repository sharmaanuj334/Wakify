package com.example.wakify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val requestCode = intent?.getIntExtra("requestCode", -1) ?: -1
        Log.d("AlarmReceiver", "Alarm triggered with request code: $requestCode")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("requestCode", requestCode)
        }
        context.startForegroundService(serviceIntent)
    }
}
