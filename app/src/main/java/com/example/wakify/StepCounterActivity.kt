package com.example.wakify

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
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

    private var ringtone: Ringtone? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter)

        stepsTextView = findViewById(R.id.stepsTextView)
        instructionTextView = findViewById(R.id.instructionTextView)
        instructionTextView.text = "Walk $targetSteps steps to dismiss the alarm"

        // Check for Activity Recognition permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), PERMISSION_REQUEST_CODE)
                return  // Wait for permission result before continuing
            }
        }

        setupSensorAndAlarm()
    }

    private fun setupSensorAndAlarm() {
        // Wake up and unlock screen
        wakeAndUnlockScreen()

        // Initialize step detector sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor == null) {
            Toast.makeText(this, "Step Detector not available!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("StepCounter", "Step detector sensor registered")

        // Play alarm sound
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone?.play()
    }

    private fun wakeAndUnlockScreen() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "wakify:alarmWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)  // 10 minutes

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("wakify:alarmKeyguardLock")
        keyguardLock.disableKeyguard()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Activity Recognition permission granted", Toast.LENGTH_SHORT).show()
                setupSensorAndAlarm()  // Continue setup now that permission is granted
            } else {
                Toast.makeText(this, "Permission denied. Steps won't be counted.", Toast.LENGTH_LONG).show()
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
        ringtone?.stop()
        wakeLock?.release()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepsTaken++
            stepsTextView.text = "Steps taken: $stepsTaken / $targetSteps"
            Log.d("StepCounter", "Step detected. Total: $stepsTaken")

            if (stepsTaken >= targetSteps) {
                Toast.makeText(this, "Alarm dismissed!", Toast.LENGTH_SHORT).show()
                ringtone?.stop()
                finish()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
