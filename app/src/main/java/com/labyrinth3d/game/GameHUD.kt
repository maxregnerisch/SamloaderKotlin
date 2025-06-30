package com.labyrinth3d.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class GameHUD @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Game state data
    private var playerHealth = 100f
    private var maxPlayerHealth = 100f
    private var playerLevel = 1
    private var playerExperience = 0
    private var experienceForNextLevel = 100
    private var score = 0
    private var lives = 3
    private var coins = 0
    private var gameTime = 0f
    private var isPaused = false
    
    // Player position for minimap
    private var playerX = 0f
    private var playerZ = 0f
    
    // Minimap data
    private val minimapSize = 150f
    private val minimapWalls = mutableListOf<RectF>()
    private val minimapEnemies = mutableListOf<PointF>()
    private val minimapCoins = mutableListOf<PointF>()
    
    // Paint objects
    private val healthBarPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val healthBarBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }
    
    private val smallTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
        setShadowLayer(1f, 1f, 1f, Color.BLACK)
    }
    
    private val minimapPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val minimapBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    
    // HUD layout
    private var hudWidth = 0f
    private var hudHeight = 0f
    private val padding = 20f
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hudWidth = w.toFloat()
        hudHeight = h.toFloat()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw semi-transparent background for HUD elements
        drawHUDBackground(canvas)
        
        // Draw health bar
        drawHealthBar(canvas)
        
        // Draw experience bar
        drawExperienceBar(canvas)
        
        // Draw game stats
        drawGameStats(canvas)
        
        // Draw minimap
        drawMinimap(canvas)
        
        // Draw special effects
        drawSpecialEffects(canvas)
        
        // Draw pause overlay if needed
        if (isPaused) {
            drawPauseOverlay(canvas)
        }
    }
    
    private fun drawHUDBackground(canvas: Canvas) {
        // Top HUD background
        val topHudPaint = Paint().apply {
            color = Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, hudWidth, 120f, topHudPaint)
        
        // Bottom HUD background (for controls area)
        canvas.drawRect(0f, hudHeight - 80f, hudWidth, hudHeight, topHudPaint)
    }
    
    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = 200f
        val barHeight = 20f
        val x = padding
        val y = padding
        
        // Health bar background
        healthBarPaint.color = Color.argb(150, 100, 0, 0)
        canvas.drawRoundRect(x, y, x + barWidth, y + barHeight, 10f, 10f, healthBarPaint)
        
        // Health bar fill
        val healthPercent = playerHealth / maxPlayerHealth
        val fillWidth = barWidth * healthPercent
        
        // Color based on health level
        healthBarPaint.color = when {
            healthPercent > 0.6f -> Color.argb(200, 0, 200, 0)
            healthPercent > 0.3f -> Color.argb(200, 255, 165, 0)
            else -> Color.argb(200, 200, 0, 0)
        }
        
        canvas.drawRoundRect(x, y, x + fillWidth, y + barHeight, 10f, 10f, healthBarPaint)
        
        // Health bar border
        canvas.drawRoundRect(x, y, x + barWidth, y + barHeight, 10f, 10f, healthBarBorderPaint)
        
        // Health text
        val healthText = "${playerHealth.toInt()}/${maxPlayerHealth.toInt()}"
        canvas.drawText(healthText, x + barWidth + 10f, y + barHeight - 2f, smallTextPaint)
        
        // Health icon
        canvas.drawText("❤", x - 30f, y + barHeight - 2f, textPaint)
    }
    
    private fun drawExperienceBar(canvas: Canvas) {
        val barWidth = 150f
        val barHeight = 15f
        val x = padding
        val y = padding + 35f
        
        // Experience bar background
        healthBarPaint.color = Color.argb(150, 0, 0, 100)
        canvas.drawRoundRect(x, y, x + barWidth, y + barHeight, 8f, 8f, healthBarPaint)
        
        // Experience bar fill
        val expPercent = playerExperience.toFloat() / experienceForNextLevel.toFloat()
        val fillWidth = barWidth * expPercent
        
        healthBarPaint.color = Color.argb(200, 0, 100, 255)
        canvas.drawRoundRect(x, y, x + fillWidth, y + barHeight, 8f, 8f, healthBarPaint)
        
        // Experience bar border
        canvas.drawRoundRect(x, y, x + barWidth, y + barHeight, 8f, 8f, healthBarBorderPaint)
        
        // Level and experience text
        val levelText = "Lv.$playerLevel"
        canvas.drawText(levelText, x + barWidth + 10f, y + barHeight - 1f, smallTextPaint)
        
        val expText = "$playerExperience/$experienceForNextLevel"
        canvas.drawText(expText, x, y + barHeight + 18f, smallTextPaint)
    }
    
    private fun drawGameStats(canvas: Canvas) {
        val rightSide = hudWidth - padding
        
        // Score
        val scoreText = "Score: $score"
        val scoreWidth = textPaint.measureText(scoreText)
        canvas.drawText(scoreText, rightSide - scoreWidth, padding + 30f, textPaint)
        
        // Lives
        val livesY = padding + 65f
        for (i in 0 until lives) {
            canvas.drawText("♥", rightSide - 40f - (i * 35f), livesY, textPaint)
        }
        
        // Coins
        val coinsText = "💰 $coins"
        val coinsWidth = textPaint.measureText(coinsText)
        canvas.drawText(coinsText, rightSide - coinsWidth, livesY + 35f, textPaint)
        
        // Game time
        val minutes = (gameTime / 60).toInt()
        val seconds = (gameTime % 60).toInt()
        val timeText = String.format("%02d:%02d", minutes, seconds)
        val timeWidth = smallTextPaint.measureText(timeText)
        canvas.drawText(timeText, rightSide - timeWidth, padding + 100f, smallTextPaint)
    }
    
    private fun drawMinimap(canvas: Canvas) {
        val minimapX = hudWidth - minimapSize - padding
        val minimapY = hudHeight - minimapSize - padding - 100f
        
        // Minimap background
        minimapPaint.color = Color.argb(150, 0, 0, 0)
        canvas.drawRoundRect(
            minimapX, minimapY, 
            minimapX + minimapSize, minimapY + minimapSize,
            10f, 10f, minimapPaint
        )
        
        // Minimap border
        canvas.drawRoundRect(
            minimapX, minimapY,
            minimapX + minimapSize, minimapY + minimapSize,
            10f, 10f, minimapBorderPaint
        )
        
        // Draw walls
        minimapPaint.color = Color.argb(200, 100, 100, 100)
        for (wall in minimapWalls) {
            val scaledWall = RectF(
                minimapX + wall.left * minimapSize / 20f,
                minimapY + wall.top * minimapSize / 20f,
                minimapX + wall.right * minimapSize / 20f,
                minimapY + wall.bottom * minimapSize / 20f
            )
            canvas.drawRect(scaledWall, minimapPaint)
        }
        
        // Draw coins
        minimapPaint.color = Color.YELLOW
        for (coin in minimapCoins) {
            val x = minimapX + (coin.x + 10f) * minimapSize / 20f
            val y = minimapY + (coin.y + 10f) * minimapSize / 20f
            canvas.drawCircle(x, y, 3f, minimapPaint)
        }
        
        // Draw enemies
        minimapPaint.color = Color.RED
        for (enemy in minimapEnemies) {
            val x = minimapX + (enemy.x + 10f) * minimapSize / 20f
            val y = minimapY + (enemy.y + 10f) * minimapSize / 20f
            canvas.drawCircle(x, y, 4f, minimapPaint)
        }
        
        // Draw player
        minimapPaint.color = Color.BLUE
        val playerMinimapX = minimapX + (playerX + 10f) * minimapSize / 20f
        val playerMinimapY = minimapY + (playerZ + 10f) * minimapSize / 20f
        canvas.drawCircle(playerMinimapX, playerMinimapY, 5f, minimapPaint)
        
        // Player direction indicator
        minimapPaint.color = Color.WHITE
        val directionLength = 8f
        val directionX = playerMinimapX + cos(Math.toRadians(0.0)) * directionLength
        val directionY = playerMinimapY + sin(Math.toRadians(0.0)) * directionLength
        canvas.drawLine(
            playerMinimapX, playerMinimapY,
            directionX.toFloat(), directionY.toFloat(),
            minimapPaint
        )
        
        // Minimap title
        canvas.drawText("Map", minimapX, minimapY - 10f, smallTextPaint)
    }
    
    private fun drawSpecialEffects(canvas: Canvas) {
        // Draw damage flash effect
        if (damageFlashTime > 0f) {
            val alpha = (damageFlashTime * 255).toInt().coerceIn(0, 100)
            val flashPaint = Paint().apply {
                color = Color.argb(alpha, 255, 0, 0)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, hudWidth, hudHeight, flashPaint)
        }
        
        // Draw heal flash effect
        if (healFlashTime > 0f) {
            val alpha = (healFlashTime * 255).toInt().coerceIn(0, 80)
            val flashPaint = Paint().apply {
                color = Color.argb(alpha, 0, 255, 0)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, hudWidth, hudHeight, flashPaint)
        }
        
        // Draw level up effect
        if (levelUpTime > 0f) {
            val scale = 1f + levelUpTime * 0.5f
            val alpha = (levelUpTime * 255).toInt().coerceIn(0, 255)
            
            val levelUpPaint = Paint().apply {
                color = Color.argb(alpha, 255, 215, 0)
                textSize = 60f * scale
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(5f, 2f, 2f, Color.BLACK)
            }
            
            canvas.drawText("LEVEL UP!", hudWidth / 2f, hudHeight / 2f, levelUpPaint)
        }
    }
    
    private fun drawPauseOverlay(canvas: Canvas) {
        // Semi-transparent overlay
        val overlayPaint = Paint().apply {
            color = Color.argb(150, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, hudWidth, hudHeight, overlayPaint)
        
        // Pause text
        val pausePaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
        }
        
        canvas.drawText("PAUSED", hudWidth / 2f, hudHeight / 2f - 50f, pausePaint)
        
        // Instructions
        val instructionPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        
        canvas.drawText("Tap to Resume", hudWidth / 2f, hudHeight / 2f + 20f, instructionPaint)
    }
    
    // Flash effect timers
    private var damageFlashTime = 0f
    private var healFlashTime = 0f
    private var levelUpTime = 0f
    
    fun update(deltaTime: Float) {
        // Update flash effects
        if (damageFlashTime > 0f) {
            damageFlashTime -= deltaTime * 3f
            if (damageFlashTime <= 0f) damageFlashTime = 0f
            invalidate()
        }
        
        if (healFlashTime > 0f) {
            healFlashTime -= deltaTime * 2f
            if (healFlashTime <= 0f) healFlashTime = 0f
            invalidate()
        }
        
        if (levelUpTime > 0f) {
            levelUpTime -= deltaTime
            if (levelUpTime <= 0f) levelUpTime = 0f
            invalidate()
        }
    }
    
    // Public methods to update HUD data
    fun updatePlayerStats(health: Float, maxHealth: Float, level: Int, experience: Int, expForNext: Int) {
        playerHealth = health
        maxPlayerHealth = maxHealth
        playerLevel = level
        playerExperience = experience
        experienceForNextLevel = expForNext
        invalidate()
    }
    
    fun updateGameStats(newScore: Int, newLives: Int, newCoins: Int, time: Float) {
        score = newScore
        lives = newLives
        coins = newCoins
        gameTime = time
        invalidate()
    }
    
    fun updatePlayerPosition(x: Float, z: Float) {
        playerX = x
        playerZ = z
        invalidate()
    }
    
    fun updateMinimap(walls: List<RectF>, enemies: List<PointF>, coins: List<PointF>) {
        minimapWalls.clear()
        minimapWalls.addAll(walls)
        minimapEnemies.clear()
        minimapEnemies.addAll(enemies)
        minimapCoins.clear()
        minimapCoins.addAll(coins)
        invalidate()
    }
    
    fun setPaused(paused: Boolean) {
        isPaused = paused
        invalidate()
    }
    
    fun showDamageFlash() {
        damageFlashTime = 0.5f
        invalidate()
    }
    
    fun showHealFlash() {
        healFlashTime = 0.3f
        invalidate()
    }
    
    fun showLevelUpEffect() {
        levelUpTime = 2f
        invalidate()
    }
    
    // Utility methods
    fun getHealthPercent() = playerHealth / maxPlayerHealth
    fun getExperiencePercent() = playerExperience.toFloat() / experienceForNextLevel.toFloat()
}

