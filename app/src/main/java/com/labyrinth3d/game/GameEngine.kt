package com.labyrinth3d.game

import android.content.Context
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameEngine(private val context: Context) {
    
    interface GameCallback {
        fun onScoreChanged(score: Int)
        fun onLivesChanged(lives: Int)
        fun onGameOver(finalScore: Int)
        fun onVibrate(duration: Long)
        fun onCoinCollected()
        fun onPlayerDamaged(damage: Float)
        fun onPlayerHealed(amount: Float)
        fun onExplosion(x: Float, y: Float, z: Float)
    }
    
    private var callback: GameCallback? = null
    private var isPaused = false
    
    // Game state
    private var score = 0
    private var lives = 3
    private var gameOver = false
    
    // Ball physics
    private var ballX = 0f
    private var ballZ = 0f
    private var ballVelX = 0f
    private var ballVelZ = 0f
    private var tiltX = 0f
    private var tiltZ = 0f
    
    // Physics constants
    private val gravity = 0.02f
    private val friction = 0.95f
    private val maxVelocity = 0.3f
    private val ballRadius = 0.3f
    
    // Labyrinth boundaries
    private val labyrinthSize = 10f
    private val wallThickness = 0.5f
    
    // Enhanced game components
    private var playerAvatar: PlayerAvatar? = null
    private var particleSystem: ParticleSystem? = null
    private var soundManager: SoundManager? = null
    private var coins = 0
    private var gameTime = 0f
    
    fun setGameCallback(callback: GameCallback) {
        this.callback = callback
        callback.onScoreChanged(score)
        callback.onLivesChanged(lives)
    }
    
    fun setPaused(paused: Boolean) {
        isPaused = paused
    }
    
    fun update() {
        if (isPaused || gameOver) return
        
        // Apply tilt to ball velocity
        ballVelX += tiltX * gravity
        ballVelZ += tiltZ * gravity
        
        // Limit velocity
        ballVelX = max(-maxVelocity, min(maxVelocity, ballVelX))
        ballVelZ = max(-maxVelocity, min(maxVelocity, ballVelZ))
        
        // Update ball position
        ballX += ballVelX
        ballZ += ballVelZ
        
        // Check wall collisions
        checkWallCollisions()
        
        // Apply friction
        ballVelX *= friction
        ballVelZ *= friction
    }
    
    private fun checkWallCollisions() {
        // Outer walls
        if (ballX - ballRadius < -labyrinthSize) {
            ballX = -labyrinthSize + ballRadius
            ballVelX = abs(ballVelX) * 0.5f
            callback?.onVibrate(100)
        }
        if (ballX + ballRadius > labyrinthSize) {
            ballX = labyrinthSize - ballRadius
            ballVelX = -abs(ballVelX) * 0.5f
            callback?.onVibrate(100)
        }
        if (ballZ - ballRadius < -labyrinthSize) {
            ballZ = -labyrinthSize + ballRadius
            ballVelZ = abs(ballVelZ) * 0.5f
            callback?.onVibrate(100)
        }
        if (ballZ + ballRadius > labyrinthSize) {
            ballZ = labyrinthSize - ballRadius
            ballVelZ = -abs(ballVelZ) * 0.5f
            callback?.onVibrate(100)
        }
        
        // Internal maze walls (simplified maze structure)
        checkInternalWalls()
    }
    
    private fun checkInternalWalls() {
        // Simple maze walls - you can expand this for more complex mazes
        val walls = listOf(
            // Horizontal walls (x1, z1, x2, z2)
            floatArrayOf(-5f, -2f, 0f, -2f),
            floatArrayOf(2f, -2f, 7f, -2f),
            floatArrayOf(-7f, 2f, -2f, 2f),
            floatArrayOf(0f, 2f, 5f, 2f),
            floatArrayOf(-3f, 5f, 3f, 5f),
            
            // Vertical walls
            floatArrayOf(-2f, -7f, -2f, -4f),
            floatArrayOf(2f, -5f, 2f, 0f),
            floatArrayOf(5f, -7f, 5f, -4f),
            floatArrayOf(-5f, 0f, -5f, 4f),
            floatArrayOf(3f, 3f, 3f, 7f)
        )
        
        for (wall in walls) {
            if (checkWallCollision(wall[0], wall[1], wall[2], wall[3])) {
                callback?.onVibrate(100)
            }
        }
    }
    
    private fun checkWallCollision(x1: Float, z1: Float, x2: Float, z2: Float): Boolean {
        // Check collision with wall segment
        val wallCenterX = (x1 + x2) / 2
        val wallCenterZ = (z1 + z2) / 2
        val wallWidth = abs(x2 - x1) + wallThickness
        val wallHeight = abs(z2 - z1) + wallThickness
        
        if (ballX + ballRadius > wallCenterX - wallWidth/2 &&
            ballX - ballRadius < wallCenterX + wallWidth/2 &&
            ballZ + ballRadius > wallCenterZ - wallHeight/2 &&
            ballZ - ballRadius < wallCenterZ + wallHeight/2) {
            
            // Determine collision side and bounce
            val overlapX = min(ballX + ballRadius - (wallCenterX - wallWidth/2),
                              (wallCenterX + wallWidth/2) - (ballX - ballRadius))
            val overlapZ = min(ballZ + ballRadius - (wallCenterZ - wallHeight/2),
                              (wallCenterZ + wallHeight/2) - (ballZ - ballRadius))
            
            if (overlapX < overlapZ) {
                // Horizontal collision
                if (ballX < wallCenterX) {
                    ballX = wallCenterX - wallWidth/2 - ballRadius
                } else {
                    ballX = wallCenterX + wallWidth/2 + ballRadius
                }
                ballVelX = -ballVelX * 0.5f
            } else {
                // Vertical collision
                if (ballZ < wallCenterZ) {
                    ballZ = wallCenterZ - wallHeight/2 - ballRadius
                } else {
                    ballZ = wallCenterZ + wallHeight/2 + ballRadius
                }
                ballVelZ = -ballVelZ * 0.5f
            }
            return true
        }
        return false
    }
    
    fun updateTilt(x: Float, z: Float) {
        tiltX = x
        tiltZ = z
    }
    
    fun onTouch(x: Float, y: Float) {
        // Handle touch events if needed
    }
    
    fun addScore(points: Int) {
        score += points
        callback?.onScoreChanged(score)
    }
    
    fun loseLife() {
        lives--
        callback?.onLivesChanged(lives)
        if (lives <= 0) {
            gameOver = true
            callback?.onGameOver(score)
        }
    }
    
    fun showMathQuestion(mathQuestion: MathQuestion) {
        // This would show a math question dialog
        // For now, just add score
        addScore(20)
    }
    
    fun onVibrate(duration: Long) {
        callback?.onVibrate(duration)
    }
    
    // Enhanced game methods
    fun setPlayerAvatar(avatar: PlayerAvatar) {
        playerAvatar = avatar
    }
    
    fun setParticleSystem(system: ParticleSystem) {
        particleSystem = system
    }
    
    fun setSoundManager(manager: SoundManager) {
        soundManager = manager
    }
    
    fun updatePlayerPosition(x: Float, y: Float, z: Float) {
        ballX = x
        ballZ = z
        // Update ball position for collision detection
    }
    
    fun performAction() {
        // Implement special action (attack, interact, etc.)
        playerAvatar?.let { avatar ->
            // Add action logic here
            callback?.onVibrate(100)
        }
    }
    
    fun useSpecialAbility() {
        // Implement special ability
        playerAvatar?.let { avatar ->
            if (avatar.getLevel() >= 2) {
                // Heal player
                callback?.onPlayerHealed(20f)
                callback?.onVibrate(150)
            }
        }
    }
    
    fun onCoinCollected() {
        coins++
        callback?.onCoinCollected()
        callback?.onVibrate(50)
    }
    
    fun onPlayerDamaged(damage: Float) {
        callback?.onPlayerDamaged(damage)
        callback?.onVibrate(200)
    }
    
    fun onExplosion(x: Float, y: Float, z: Float) {
        callback?.onExplosion(x, y, z)
        callback?.onVibrate(300)
    }
    
    fun addExplosionEffect(x: Float, y: Float, z: Float) {
        particleSystem?.emit(ParticleSystem.ParticleType.EXPLOSION, x, y, z, 30)
    }
    
    fun getCoins() = coins
    fun getGameTime() = gameTime
    
    fun addCoins(amount: Int) {
        coins += amount
        callback?.onCoinCollected()
    }
    
    fun isPaused() = isPaused
    
    // Getters
    fun getBallX() = ballX
    fun getBallZ() = ballZ
    fun getScore() = score
    fun getLives() = lives
    fun getCoins() = coins
    fun isGameOver() = gameOver
}
