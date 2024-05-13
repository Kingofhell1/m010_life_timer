package com.example.m010_life_timer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.m010_life_timer.databinding.ActivityMainBinding


private const val TIMER_VALUE = "timerValue"
private const val TIMER_IS_ACTIVE = "timerIsActive"

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var handler = Handler(Looper.getMainLooper())

    private var timerValue = 10
    private var timeCounter = 10
    private var timerIsActive = false

    private lateinit var timerThread: Thread
    private var timerThreadStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            timerValue = savedInstanceState.getInt("timerValue")
            timerIsActive = savedInstanceState.getBoolean("timerIsActive")
            Log.d("msg", "onCreate: после изменения $timerValue")
            timeCounter = timerValue
            handler.post {
                updateTimer()
                if (timerIsActive) startTimer()
            }
        }

        handler.post {

            binding.slider.addOnChangeListener { _, value, _ ->
                timerValue = value.toInt()
                timeCounter = timerValue
                updateUI()
            }

            binding.buttonStart.setOnClickListener {
                    startTimer()
            }
        }

    }

    private fun updateTimer() {
        binding.timer.text = timeCounter.toString()
        binding.progressBar.progress = timeCounter
    }

    private fun updateUI(){
        when(timerIsActive){
            true -> {
                binding.slider.isEnabled = false
                binding.buttonStart.visibility = View.GONE
                binding.buttonStop.visibility = View.VISIBLE
            }
            false ->{
                binding.slider.isEnabled = true
                binding.buttonStart.visibility = View.VISIBLE
                binding.buttonStop.visibility = View.GONE
            }
        }

            binding.progressBar.max = binding.slider.value.toInt()
                    binding.progressBar.progress = timeCounter
                    binding.timer.text = timerValue.toString()
    }

    private fun startTimer() {
        timerThread = Thread {
            while (!timerThreadStop && timeCounter > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                binding.buttonStop.setOnClickListener {
                        timerIsActive = false
                        stopTimerThread()
                }
                
                timeCounter--

                handler.post {
                    updateTimer()
                }
            }

            if (timeCounter == 0) timerIsActive = false
            handler.post { finishTimer() }
        }

        timerIsActive = true
        handler.post { updateUI() }
        timerThread.start()
    }

    private fun finishTimer() {
        if (!timerIsActive) {
            Toast.makeText(this, "Timer Task Finished", Toast.LENGTH_SHORT).show()
        }
        timerValue = binding.slider.value.toInt()
        timeCounter = timerValue
        timerThreadStop = false
        handler.post { updateUI() }
    }

    private fun stopTimerThread() {
//        timerThreadStop = true
        timerThread.interrupt()
    }

    override fun onPause() {
        super.onPause()
        try {
            stopTimerThread()
        } catch (ex: Exception) {
            Log.d("msg", "onPause: $ex")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(TIMER_VALUE, timeCounter)
        outState.putBoolean(TIMER_IS_ACTIVE, timerIsActive)
        super.onSaveInstanceState(outState)
    }

}
