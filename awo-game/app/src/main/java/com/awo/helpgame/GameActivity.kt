package com.awo.helpgame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvPeopleHelped: TextView
    private lateinit var btnHelp: Button
    private lateinit var btnRestart: Button
    private lateinit var btnBackToMenu: Button

    private var score = 0
    private var level = 1
    private var peopleHelped = 0
    private var clickMultiplier = 1

    private val handler = Handler(Looper.getMainLooper())
    private var gameRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        setupClickListeners()
        startGame()
    }

    private fun initViews() {
        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvPeopleHelped = findViewById(R.id.tvPeopleHelped)
        btnHelp = findViewById(R.id.btnHelp)
        btnRestart = findViewById(R.id.btnRestart)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)
    }

    private fun setupClickListeners() {
        btnHelp.setOnClickListener {
            helpPeople()
        }

        btnRestart.setOnClickListener {
            restartGame()
        }

        btnBackToMenu.setOnClickListener {
            finish()
        }
    }

    private fun startGame() {
        updateUI()
        startAutoHelp()
    }

    private fun helpPeople() {
        val helpAmount = clickMultiplier + Random.nextInt(1, 4)
        peopleHelped += helpAmount
        score += helpAmount * 10
        
        // Animate button
        animateHelpButton()
        
        // Show floating text
        showFloatingText("+$helpAmount Menschen geholfen!")
        
        // Check for level up
        checkLevelUp()
        
        updateUI()
    }

    private fun animateHelpButton() {
        val scaleX = ObjectAnimator.ofFloat(btnHelp, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnHelp, "scaleY", 1f, 1.2f, 1f)
        val rotation = ObjectAnimator.ofFloat(btnHelp, "rotation", 0f, 10f, -10f, 0f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, rotation)
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun showFloatingText(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun checkLevelUp() {
        val newLevel = (peopleHelped / 50) + 1
        if (newLevel > level) {
            level = newLevel
            clickMultiplier = level
            Toast.makeText(this, "Level $level erreicht! Multiplier: x$clickMultiplier", Toast.LENGTH_LONG).show()
        }
    }

    private fun startAutoHelp() {
        gameRunnable = object : Runnable {
            override fun run() {
                // Auto help every 3 seconds (passive income)
                if (level > 1) {
                    val autoHelp = level - 1
                    peopleHelped += autoHelp
                    score += autoHelp * 5
                    updateUI()
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(gameRunnable!!, 3000)
    }

    private fun updateUI() {
        tvScore.text = getString(R.string.score, score)
        tvLevel.text = getString(R.string.level, level)
        tvPeopleHelped.text = getString(R.string.people_helped, peopleHelped)
    }

    private fun restartGame() {
        score = 0
        level = 1
        peopleHelped = 0
        clickMultiplier = 1
        updateUI()
        Toast.makeText(this, "Spiel neu gestartet!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameRunnable?.let { handler.removeCallbacks(it) }
    }
}

