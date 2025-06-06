package com.example.wakify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val requestCode = intent?.getIntExtra("requestCode", -1) ?: -1

        Toast.makeText(context, "Alarm #$requestCode Triggered!", Toast.LENGTH_SHORT).show()
        Log.d("AlarmReceiver", "Alarm triggered with request code: $requestCode")

        // Start StepCounterActivity when alarm goes off
        val stepIntent = Intent(context, StepCounterActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context?.startActivity(stepIntent)
    }
}
