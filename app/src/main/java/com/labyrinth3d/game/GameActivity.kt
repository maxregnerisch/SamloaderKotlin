package com.labyrinth3d.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GameActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var gameView: GameGLSurfaceView
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var pauseButton: Button
    private lateinit var gameOverlay: View
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var vibrator: Vibrator
    
    private var gameEngine: GameEngine? = null
    private var isPaused = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        setupFullscreen()
        initializeViews()
        setupSensors()
        setupGame()
    }
    
    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    private fun initializeViews() {
        gameView = findViewById(R.id.gameView)
        scoreText = findViewById(R.id.scoreText)
        livesText = findViewById(R.id.livesText)
        pauseButton = findViewById(R.id.pauseButton)
        gameOverlay = findViewById(R.id.gameOverlay)
        
        pauseButton.setOnClickListener {
            togglePause()
        }
    }
    
    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private fun setupGame() {
        gameEngine = GameEngine(this)
        gameView.setGameEngine(gameEngine!!)
        
        gameEngine?.setGameCallback(object : GameEngine.GameCallback {
            override fun onScoreChanged(score: Int) {
                runOnUiThread {
                    scoreText.text = "Score: $score"
                }
            }
            
            override fun onLivesChanged(lives: Int) {
                runOnUiThread {
                    livesText.text = "Lives: $lives"
                }
            }
            
            override fun onGameOver(finalScore: Int) {
                runOnUiThread {
                    handleGameOver(finalScore)
                }
            }
            
            override fun onVibrate(duration: Long) {
                vibrator.vibrate(duration)
            }
        })
    }
    
    private fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            gameView.onPause()
            pauseButton.text = "Resume"
        } else {
            gameView.onResume()
            pauseButton.text = "Pause"
        }
        gameEngine?.setPaused(isPaused)
    }
    
    private fun handleGameOver(finalScore: Int) {
        // Save high score
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val currentHighScore = sharedPref.getInt("high_score", 0)
        if (finalScore > currentHighScore) {
            sharedPref.edit().putInt("high_score", finalScore).apply()
        }
        
        // Show game over dialog or return to main menu
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        gameView.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    
    override fun onPause() {
        super.onPause()
        gameView.onPause()
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && !isPaused) {
            val x = event.values[0]
            val y = event.values[1]
            gameEngine?.updateTilt(-x, y) // Invert X for natural movement
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}

