package com.labyrinth3d.game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var playButton: Button
    private lateinit var highScoreText: TextView
    private lateinit var titleText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupFullscreen()
        initializeViews()
        setupClickListeners()
        loadHighScore()
    }
    
    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    private fun initializeViews() {
        playButton = findViewById(R.id.playButton)
        highScoreText = findViewById(R.id.highScoreText)
        titleText = findViewById(R.id.titleText)
        
        // Animate title
        titleText.alpha = 0f
        titleText.animate()
            .alpha(1f)
            .setDuration(2000)
            .start()
    }
    
    private fun setupClickListeners() {
        playButton.setOnClickListener {
            startGame()
        }
    }
    
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
    
    private fun loadHighScore() {
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val highScore = sharedPref.getInt("high_score", 0)
        highScoreText.text = "High Score: $highScore"
    }
    
    override fun onResume() {
        super.onResume()
        loadHighScore()
    }
}

