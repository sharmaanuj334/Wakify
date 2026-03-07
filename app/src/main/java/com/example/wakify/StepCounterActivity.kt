package com.example.wakify

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StepCounterActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepsTaken: Int = 0
    private val targetSteps = 20

    private lateinit var stepsTextView: TextView
    private lateinit var instructionTextView: TextView

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and turn screen on
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_step_counter)

        stepsTextView = findViewById(R.id.stepsTextView)
        instructionTextView = findViewById(R.id.instructionTextView)
        instructionTextView.text = "Walk $targetSteps steps to dismiss the alarm"

        // Check for Activity Recognition permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), PERMISSION_REQUEST_CODE)
                return
            }
        }

        setupSensor()
    }

    private fun setupSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Toast.makeText(this, "Step Detector not available!", Toast.LENGTH_LONG).show()
            stopAlarmService()
            finish()
            return
        }

        Log.d("StepCounter", "Step detector sensor registered")
    }

    private fun stopAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        startService(serviceIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Activity Recognition permission granted", Toast.LENGTH_SHORT).show()
                setupSensor()
            } else {
                Toast.makeText(this, "Permission denied. Steps won't be counted.", Toast.LENGTH_LONG).show()
                stopAlarmService()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.also {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepsTaken++
            stepsTextView.text = "Steps taken: $stepsTaken / $targetSteps"
            Log.d("StepCounter", "Step detected. Total: $stepsTaken")

            if (stepsTaken >= targetSteps) {
                Toast.makeText(this, "Alarm dismissed!", Toast.LENGTH_SHORT).show()
                stopAlarmService()
                finish()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
