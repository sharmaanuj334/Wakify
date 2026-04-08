package com.example.wakify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private val alarmList = mutableListOf<Alarm>()
    private lateinit var adapter: AlarmAdapter
    private lateinit var requestCodeGen: AtomicInteger
    private val SET_ALARM_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var alarmCountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load persisted alarms and request code counter
        alarmList.addAll(AlarmStorage.loadAlarms(this))
        requestCodeGen = AtomicInteger(AlarmStorage.loadNextRequestCode(this))

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.alarmList)
        val fab = findViewById<FloatingActionButton>(R.id.addAlarmFab)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        alarmCountText = findViewById(R.id.alarmCountText)

        adapter = AlarmAdapter(alarmList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.listener = object : AlarmAdapter.OnAlarmClickListener {
            override fun onAlarmClick(position: Int) {
                val alarm = alarmList[position]
                AlarmScheduler.cancelAlarm(this@MainActivity, alarm)

                alarmList.removeAt(position)
                adapter.notifyItemRemoved(position)
                AlarmStorage.saveAlarms(this@MainActivity, alarmList)

                updateEmptyState()

                Toast.makeText(this@MainActivity, "Alarm deleted", Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(this, "Please allow exact alarm permission", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Request battery optimization exemption so alarms fire reliably in Doze
        requestBatteryOptimizationExemption()

        fab.setOnClickListener {
            val intent = Intent(this, SetAlarmActivity::class.java)
            startActivityForResult(intent, SET_ALARM_REQUEST_CODE)
        }

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (alarmList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            alarmCountText.text = "No alarms set"
        } else {
            emptyStateLayout.visibility = View.GONE
            val count = alarmList.size
            alarmCountText.text = if (count == 1) "1 alarm set" else "$count alarms set"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SET_ALARM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val hour = data.getIntExtra("hour", -1)
            val minute = data.getIntExtra("minute", -1)

            if (hour >= 0 && minute >= 0) {
                val code = requestCodeGen.getAndIncrement()
                AlarmStorage.saveNextRequestCode(this, requestCodeGen.get())

                val alarm = Alarm(hour, minute, code)
                alarmList.add(alarm)
                adapter.notifyItemInserted(alarmList.size - 1)
                AlarmStorage.saveAlarms(this, alarmList)

                updateEmptyState()

                setExactAlarm(alarm)
            }
        }
    }

    private fun setExactAlarm(alarm: Alarm) {
        AlarmScheduler.scheduleAlarm(this, alarm)

        val displayHour = when {
            alarm.hour == 0 -> 12
            alarm.hour > 12 -> alarm.hour - 12
            else -> alarm.hour
        }
        val amPm = if (alarm.hour < 12) "AM" else "PM"
        Toast.makeText(this, "Alarm set for $displayHour:${"%02d".format(alarm.minute)} $amPm", Toast.LENGTH_SHORT).show()
    }

    private fun requestBatteryOptimizationExemption() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
