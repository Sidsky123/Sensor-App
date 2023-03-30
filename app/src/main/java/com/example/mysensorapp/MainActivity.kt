package com.example.mysensorapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor

    private lateinit var mediaPlayer: MediaPlayer
    private var state = "Still"
    private var prevState = ""
//    lateinit var durationStore:LocalDateTime
    var timeBegin:LocalDateTime= LocalDateTime.now()
    var timeEnd:LocalDateTime= LocalDateTime.now()

    val WALKING_THRESHOLD = 2.0f
    val RUNNING_THRESHOLD = 11.0f
    val DRIVING_THRESHOLD = 15.0f

    // Declare variables to keep track of accelerometer sensor readings
    val accelerometerValues = mutableListOf<FloatArray>()
    val timestampValues = mutableListOf<Long>()

    // Declare variable to store the current acceleration
    var acceleration: Float = 0.0f

    @RequiresApi(Build.VERSION_CODES.O)
    var time1 = LocalDateTime.now()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer=MediaPlayer.create(this,R.raw.running_song)
        mediaPlayer.start()
        setContentView(R.layout.activity_main)
        val current = LocalDateTime.now()
        val carddt: TextView = findViewById<TextView>(R.id.card_dt)
        val formatter1 = DateTimeFormatter.ofPattern("HH:mm ")
        val formatter2 = DateTimeFormatter.ofPattern("dd-MM-yy")
        val formatted = current.format(formatter1)
        val formatted2 = current.format(formatter2)
        carddt.text =
            "Welcome to Siddhant and Syeda's Sensor App \nThe Time Right now is $formatted" + "\n Current Date is $formatted2"


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()

        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
//    private var prevState="null"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val timestamp = event.timestamp

            // Two diffenet lists to store accelerometer and timestamp values
            accelerometerValues.add(floatArrayOf(x, y, z))
            timestampValues.add(timestamp)

            // Considering acceleration values for 5 seconds before utilizing sensor values as otherwise activites would keep changing rapidly
            if (timestampValues.last() - timestampValues.first() > 5_000_000_000L) {
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
                event.timestamp
                // Determine if the user is still or walking or running or driving based on the acceleration
                if (acceleration < 0.6f) {//0.6f is set as the value because minor movements shall not disturb detections
                    //User is still at their place
                    val dialog = CustomDialog()
                    dialog.show(supportFragmentManager, "customDialog")
                    Log.d(TAG, "onSensorChanged: Still")
                    state = "Still"
                    showToast(this, "STILL!")
                    if(prevState!="Still"&& prevState!="") //if activity changes then save the previous activity duration to DATABASE
                    {
                        var durationInSec=timerEnd(LocalDateTime.now())
                        storeinDB(durationInSec.toString(),prevState)
                        timerStart(LocalDateTime.now())
                        state="Still"
                    }
                }


                else if (acceleration < RUNNING_THRESHOLD && acceleration >= 1f) {
                    // User is walking
                    Log.d(TAG, "onSensorChanged: Walking")
                    val dialog = CustomDialogWalk()
                    dialog.show(supportFragmentManager, "customDialogWalk")
                    state = "Walking"
                    showToast(this, "WALKING!")
                    if(prevState!="Walking")//if activity changes then save the previous activity duration to DATABASE
                    {
                        var durationInSec=timerEnd(LocalDateTime.now())
                        if(prevState!="") {
                            storeinDB(durationInSec.toString(), prevState)
                        }
                        timerStart(LocalDateTime.now())
                        state="Walking"
                    }
                }
                else if (acceleration < DRIVING_THRESHOLD) {
                    // User is running
                    Log.d(TAG, "Running")
                    val dialog = CustomDialogRun()
                    dialog.show(supportFragmentManager, "customDialogRun")
                    state = "Running"
                    showToast(this, "RUNNING!")

                    if(prevState!="Running")//if activity changes then save the previous activity duration to DATABASE
                    {
                        var durationInSec=timerEnd(LocalDateTime.now())
                        if(prevState!=""){
                        storeinDB(durationInSec.toString(),prevState)
                        }
                        timerStart(LocalDateTime.now())
                        state="Running"
                    }

                } else {
                    val dialog = CustomDialogDrive()
                    dialog.show(supportFragmentManager, "customDialogDrive")
                    Log.d(TAG, "Driving")
                    state="Driving"
                    showToast(this, "DRIVING!")

                    if(prevState!="Driving")//if activity changes then save the previous activity duration to DATABASE
                    {
                        var durationInSec=timerEnd(LocalDateTime.now())
                        if(prevState!="") {
                            storeinDB(durationInSec.toString(), prevState)
                        }
                        timerStart(LocalDateTime.now())
                        state="Driving"
                    }
                }
                prevState=state //this is mainly done so that in the next loop if activity is same the time still goes on

                // Clear the lists for the next 3 second window
                accelerometerValues.clear()
                timestampValues.clear()
            }
        }
    }

    @SuppressLint("NewApi")
    fun storeinDB(slot1: String,activity:String) {
        val db = DBHelper(this, null)

        // creating variables for values
        // in name and age edit texts
        val time = LocalDateTime.now().toString()
        val slot = slot1

        // calling method to add
        // name to our database
        if(activity!="") {
            db.addName(time, slot, activity)
        }

        // Toast to message on the screen
        Toast.makeText(
            this,
            "You were $activity for  $slot seconds ",
            Toast.LENGTH_LONG
        ).show()
    }
    fun timerStart(timingBegin:LocalDateTime){
        timeBegin=timingBegin
    }

    fun timerEnd(timingEnd: LocalDateTime): Long {
        timeEnd = timingEnd
        var durationStore = Duration.between(timeBegin, timeEnd)
        return durationStore.seconds
    }
}

