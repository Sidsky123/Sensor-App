package com.example.mysensorapp

import android.content.ContentValues.TAG
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.math.sqrt
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlin.math.pow

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private var isWalking = false

    val WALKING_THRESHOLD = 2.0f
    val RUNNING_THRESHOLD = 9.0f

    // Declare variables to keep track of accelerometer sensor readings
    val accelerometerValues = mutableListOf<FloatArray>()
    val timestampValues = mutableListOf<Long>()

    // Declare variable to store the current acceleration
    var acceleration: Float = 0.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
//            if (it.sensor == accelerometerSensor) {
//                val acceleration = sqrt(it.values[0] * it.values[0] + it.values[1] * it.values[1] + it.values[2] * it.values[2])
//                if (acceleration > 11) {
//                    isWalking = true
//                    showToast(this, "WALKING!")
//                    // Do something when walking is detected
//                } else if (acceleration < 11) {
//                    isWalking = false
//                    val dialog= CustomDialog()
//                    dialog.show(supportFragmentManager,"customDialog")
//                    showToast(this, "STILL!")
//                    // Do something when stillness is detected
//                }
//            }
//            val x = event.values[0]
//            val y = event.values[1]
//            val z = event.values[2]
//            val timestamp = event.timestamp
//
//            // Add the accelerometer values and timestamp to their respective lists
//            accelerometerValues.add(floatArrayOf(x, y, z))
//            timestampValues.add(timestamp)
//
//            // Remove any old accelerometer values and timestamps
//            val cutoffTimestamp = timestamp - 50_000_000_000L
//            while (timestampValues.isNotEmpty() && timestampValues[0] < cutoffTimestamp) {
//                accelerometerValues.removeAt(0)
//                timestampValues.removeAt(0)
//            }
//
//            // Calculate the acceleration using the current and previous sensor readings
//            if (accelerometerValues.size >= 2) {
//                val lastTwoValues = accelerometerValues.takeLast(2)
//                val lastTwoTimestamps = timestampValues.takeLast(2)
//                val accelerationVector = FloatArray(3)
//                for (i in 0..2) {
//                    accelerationVector[i] =
//                        (lastTwoValues[1][i] - lastTwoValues[0][i]) / (lastTwoTimestamps[1] - lastTwoTimestamps[0]) * 1_000_000_000
//                }
//                acceleration = Math.sqrt(
//                    accelerationVector[0].toDouble().pow(2) +
//                            accelerationVector[1].toDouble().pow(2) +
//                            accelerationVector[2].toDouble().pow(2)
//                ).toFloat()
//
//                // Determine if the user is walking or running based on the acceleration

//            }

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val timestamp = event.timestamp

            // Add the accelerometer values and timestamp to their respective lists
            accelerometerValues.add(floatArrayOf(x, y, z))
            timestampValues.add(timestamp)

            // If we have at least 3 seconds worth of data, calculate the acceleration
            if (timestampValues.last() - timestampValues.first() > 3_000_000_000L) {
                // Calculate the acceleration over the 3 second window
                val windowStart = timestampValues.first()
                val windowEnd = timestampValues.last()
                var accelerationSum = 0.0
                for (i in 0 until accelerometerValues.size - 1) {
                    val timeDiff = timestampValues[i + 1] - timestampValues[i]
                    val accelerationVector = FloatArray(3)
                    for (j in 0..2) {
                        accelerationVector[j] =
                            (accelerometerValues[i + 1][j] - accelerometerValues[i][j]) / timeDiff * 1_000_000_000
                    }
                    val accelerationMagnitude = Math.sqrt(
                        accelerationVector[0].toDouble().pow(2) +
                                accelerationVector[1].toDouble().pow(2) +
                                accelerationVector[2].toDouble().pow(2)
                    ).toFloat()
                    accelerationSum += accelerationMagnitude
                }
                acceleration = (accelerationSum / (accelerometerValues.size - 1)).toFloat()

                // Determine if the user is walking or running based on the acceleration
                if (acceleration<1f)
                {
                    Log.d(TAG, "onSensorChanged: Still")
                }
                else if (acceleration < RUNNING_THRESHOLD && acceleration>= WALKING_THRESHOLD) {
                    // User is walking
                    Log.d(TAG, "onSensorChanged: Walking")
//                } else if (acceleration < RUNNING_THRESHOLD) {
//                    // User is jogging
//                    Log.d(TAG, "Jogging")
                } else {
                    // User is running
                    Log.d(TAG, "Running")
                }

                // Clear the lists for the next 3 second window
                accelerometerValues.clear()
                timestampValues.clear()
            }
        }
        }
    }

