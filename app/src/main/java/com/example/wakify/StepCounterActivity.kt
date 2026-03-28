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
import com.google.android.material.progressindicator.CircularProgressIndicator

class StepCounterActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepsTaken: Int = 0
    private val targetSteps = 20

    private lateinit var stepsCountText: TextView
    private lateinit var stepsLabelText: TextView
    private lateinit var instructionTextView: TextView
    private lateinit var motivationalText: TextView
    private lateinit var progressIndicator: CircularProgressIndicator

    private val PERMISSION_REQUEST_CODE = 1001

    private val motivationalMessages = arrayOf(
        "Keep walking! You can do it",
        "Almost there, keep going!",
        "You're doing great!",
        "Don't stop now!",
        "Wake up champion!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_step_counter)

        stepsCountText = findViewById(R.id.stepsCountText)
        stepsLabelText = findViewById(R.id.stepsLabelText)
        instructionTextView = findViewById(R.id.instructionTextView)
        motivationalText = findViewById(R.id.motivationalText)
        progressIndicator = findViewById(R.id.stepProgressIndicator)

        instructionTextView.text = "Walk $targetSteps steps to dismiss the alarm"
        stepsLabelText.text = "of $targetSteps steps"
        stepsCountText.text = "0"
        progressIndicator.max = 100
        progressIndicator.progress = 0

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
            val progressPercent = ((stepsTaken.toFloat() / targetSteps) * 100).toInt().coerceAtMost(100)

            stepsCountText.text = "$stepsTaken"
            stepsLabelText.text = "of $targetSteps steps"
            progressIndicator.setProgressCompat(progressPercent, true)

            // Update motivational text based on progress
            motivationalText.text = when {
                progressPercent < 25 -> motivationalMessages[0]
                progressPercent < 50 -> motivationalMessages[1]
                progressPercent < 75 -> motivationalMessages[2]
                progressPercent < 100 -> motivationalMessages[3]
                else -> motivationalMessages[4]
            }

            Log.d("StepCounter", "Step detected. Total: $stepsTaken")

            if (stepsTaken >= targetSteps) {
                Toast.makeText(this, "Alarm dismissed! Good morning!", Toast.LENGTH_SHORT).show()
                stopAlarmService()
                finish()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
