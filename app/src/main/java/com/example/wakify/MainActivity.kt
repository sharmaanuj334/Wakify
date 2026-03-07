package com.example.wakify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
    private val requestCodeGen = AtomicInteger(1)
    private val SET_ALARM_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.alarmList)
        val fab = findViewById<FloatingActionButton>(R.id.addAlarmFab)
        val emptyText = findViewById<TextView>(R.id.emptyMessage)

        adapter = AlarmAdapter(alarmList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set up click listener for deleting alarms
        adapter.listener = object : AlarmAdapter.OnAlarmClickListener {
            override fun onAlarmClick(position: Int) {
                val alarm = alarmList[position]

                // Cancel the alarm in AlarmManager
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val intent = Intent(this@MainActivity, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    alarm.requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)

                // Remove alarm from list and notify adapter
                alarmList.removeAt(position)
                adapter.notifyItemRemoved(position)

                // Show empty message if list is empty
                emptyText.visibility = if (alarmList.isEmpty()) TextView.VISIBLE else TextView.GONE

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

        fab.setOnClickListener {
            val intent = Intent(this, SetAlarmActivity::class.java)
            startActivityForResult(intent, SET_ALARM_REQUEST_CODE)
        }

        // Initially set empty message visibility
        emptyText.visibility = if (alarmList.isEmpty()) TextView.VISIBLE else TextView.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SET_ALARM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val hour = data.getIntExtra("hour", -1)
            val minute = data.getIntExtra("minute", -1)

            if (hour >= 0 && minute >= 0) {
                val requestCode = requestCodeGen.getAndIncrement()
                val alarm = Alarm(hour, minute, requestCode)
                alarmList.add(alarm)
                adapter.notifyItemInserted(alarmList.size - 1)

                findViewById<TextView>(R.id.emptyMessage).visibility =
                    if (alarmList.isEmpty()) TextView.VISIBLE else TextView.GONE

                setExactAlarm(alarm)
            }
        }
    }

    private fun setExactAlarm(alarm: Alarm) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("requestCode", alarm.requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Alarm set for ${"%02d".format(alarm.hour)}:${"%02d".format(alarm.minute)}", Toast.LENGTH_SHORT).show()
    }
}
