package com.example.wakify

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class SetAlarmActivity : AppCompatActivity() {
    private var selectedHour = -1
    private var selectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_alarm)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val setAlarmBtn = findViewById<MaterialButton>(R.id.setAlarmBtn)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        timePicker.setIs24HourView(false)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute
        }

        backButton.setOnClickListener {
            finish()
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
