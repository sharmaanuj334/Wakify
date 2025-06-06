package com.example.wakify

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SetAlarmActivity : AppCompatActivity() {
    private var selectedHour = -1
    private var selectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_alarm)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val setAlarmBtn = findViewById<Button>(R.id.setAlarmBtn)

        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute
        }

        setAlarmBtn.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("hour", selectedHour)
                putExtra("minute", selectedMinute)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // Set current time as default
        val now = Calendar.getInstance()
        timePicker.hour = now.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = now.get(Calendar.MINUTE)
        selectedHour = now.get(Calendar.HOUR_OF_DAY)
        selectedMinute = now.get(Calendar.MINUTE)
    }
}
