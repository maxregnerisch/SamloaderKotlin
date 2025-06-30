package com.labyrinth3d.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class GameActivity : AppCompatActivity(), SensorEventListener, GameControls.ControlListener {
    
    private lateinit var glSurfaceView: GameGLSurfaceView
    private lateinit var gameEngine: GameEngine
    private lateinit var gameControls: GameControls
    private lateinit var gameHUD: GameHUD
    private lateinit var sensorManager: SensorManager
    private lateinit var soundManager: SoundManager
    private lateinit var particleSystem: ParticleSystem
    private var accelerometer: Sensor? = null
    private lateinit var vibrator: Vibrator
    
    // Player avatar
    private lateinit var playerAvatar: PlayerAvatar
    
    // Control modes
    private var useAccelerometer = true
    private var useTouchControls = true
    
    // Input values
    private var inputX = 0f
    private var inputZ = 0f
    private var jumpPressed = false
    
    // Game state
    private var isPaused = false
    private var gameTime = 0f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFullscreen()
        
        // Create layout programmatically since we're adding multiple views
        val frameLayout = FrameLayout(this)
        setContentView(frameLayout)
        
        initializeViews(frameLayout)
        initializeSensors()
        initializeGame()
        initializeSound()
    }
    
    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    private fun initializeViews(container: FrameLayout) {
        // Create OpenGL surface view
        glSurfaceView = GameGLSurfaceView(this)
        container.addView(glSurfaceView)
        
        // Create game controls overlay
        gameControls = GameControls(this)
        gameControls.setControlListener(this)
        container.addView(gameControls)
        
        // Create HUD overlay
        gameHUD = GameHUD(this)
        container.addView(gameHUD)
    }
    
    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        @Suppress("DEPRECATION")
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private fun initializeGame() {
        // Create player avatar
        playerAvatar = PlayerAvatar()
        
        // Create particle system
        particleSystem = ParticleSystem()
        
        // Create game engine
        gameEngine = GameEngine(this)
        gameEngine.setPlayerAvatar(playerAvatar)
        gameEngine.setParticleSystem(particleSystem)
        glSurfaceView.setGameEngine(gameEngine)
        glSurfaceView.setPlayerAvatar(playerAvatar)
        glSurfaceView.setParticleSystem(particleSystem)
        
        gameEngine.setGameCallback(object : GameEngine.GameCallback {
            override fun onScoreChanged(score: Int) {
                runOnUiThread { updateHUD() }
            }
            
            override fun onLivesChanged(lives: Int) {
                runOnUiThread { updateHUD() }
            }
            
            override fun onGameOver(finalScore: Int) {
                runOnUiThread { handleGameOver(finalScore) }
            }
            
            override fun onVibrate(duration: Long) {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
            
            override fun onCoinCollected() {
                runOnUiThread { 
                    onCoinCollected()
                    particleSystem.emit(ParticleSystem.ParticleType.COIN_COLLECT, 
                                      playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 15)
                }
            }
            
            override fun onPlayerDamaged(damage: Float) {
                runOnUiThread { 
                    onPlayerDamaged(damage)
                    particleSystem.emit(ParticleSystem.ParticleType.DAMAGE, 
                                      playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 10)
                }
            }
            
            override fun onPlayerHealed(amount: Float) {
                runOnUiThread { 
                    onPlayerHealed(amount)
                    particleSystem.emit(ParticleSystem.ParticleType.HEAL, 
                                      playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 12)
                }
            }
            
            override fun onExplosion(x: Float, y: Float, z: Float) {
                runOnUiThread {
                    onExplosion(x, y, z)
                    particleSystem.emit(ParticleSystem.ParticleType.EXPLOSION, x, y, z, 25)
                }
            }
        })
    }
    
    private fun initializeSound() {
        soundManager = SoundManager(this)
        gameEngine.setSoundManager(soundManager)
        
        // Start background music
        soundManager.playBackgroundMusic("main_theme")
    }
    
    private fun updateHUD() {
        // Update player stats
        gameHUD.updatePlayerStats(
            playerAvatar.getHealth(),
            playerAvatar.getMaxHealth(),
            playerAvatar.getLevel(),
            playerAvatar.getExperience(),
            playerAvatar.getExperienceForNextLevel()
        )
        
        // Update game stats
        gameHUD.updateGameStats(
            gameEngine.getScore(),
            gameEngine.getLives(),
            gameEngine.getCoins(),
            gameTime
        )
        
        // Update player position for minimap
        gameHUD.updatePlayerPosition(playerAvatar.x, playerAvatar.z)
    }
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        // gameEngine.resume()
        soundManager.resumeBackgroundMusic()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        sensorManager.unregisterListener(this)
        // gameEngine.pause()
        soundManager.pauseBackgroundMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (!useAccelerometer || isPaused) return
        
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Get accelerometer values (inverted for natural movement)
                val accelX = -it.values[0] / 9.8f
                val accelZ = it.values[1] / 9.8f
                
                // Combine accelerometer with touch input if both are enabled
                if (useTouchControls) {
                    inputX = (inputX + accelX * 0.3f).coerceIn(-1f, 1f)
                    inputZ = (inputZ + accelZ * 0.3f).coerceIn(-1f, 1f)
                } else {
                    inputX = accelX
                    inputZ = accelZ
                }
                
                updatePlayerMovement()
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
    
    // GameControls.ControlListener implementation
    override fun onMovement(x: Float, z: Float) {
        if (useTouchControls && !isPaused) {
            inputX = x
            inputZ = z
            updatePlayerMovement()
        }
        soundManager.playButtonClick()
    }
    
    override fun onJump() {
        if (!isPaused) {
            jumpPressed = true
            soundManager.playJump()
            particleSystem.emit(ParticleSystem.ParticleType.SPARKLE, 
                              playerAvatar.x, playerAvatar.y, playerAvatar.z, 8)
        }
    }
    
    override fun onAction() {
        if (!isPaused) {
            // Implement special action (e.g., attack, interact)
            gameEngine.performAction()
            soundManager.playSwordSwing()
            particleSystem.emit(ParticleSystem.ParticleType.MAGIC, 
                              playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 10)
        }
    }
    
    override fun onPauseGame() {
        togglePause()
        soundManager.playButtonClick()
    }
    
    override fun onSpecialAbility() {
        if (!isPaused) {
            // Implement special ability
            gameEngine.useSpecialAbility()
            soundManager.playMagic()
            particleSystem.emit(ParticleSystem.ParticleType.MAGIC, 
                              playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 20)
        }
    }
    
    private fun updatePlayerMovement() {
        if (isPaused) return
        
        // Update player avatar with current input
        val deltaTime = 1f / 60f // Approximate frame time
        playerAvatar.update(deltaTime, inputX, inputZ, jumpPressed)
        
        // Reset jump flag
        jumpPressed = false
        
        // Update game engine with player position
        gameEngine.updatePlayerPosition(playerAvatar.x, playerAvatar.y, playerAvatar.z)
        
        // Play footstep sounds
        if (playerAvatar.getSpeed() > 0.1f) {
            soundManager.playRandomFootstep()
        }
        
        // Update game time
        gameTime += deltaTime
    }
    
    private fun togglePause() {
        isPaused = !isPaused
        gameEngine.setPaused(isPaused)
        gameHUD.setPaused(isPaused)
        
        if (isPaused) {
            soundManager.pauseBackgroundMusic()
        } else {
            soundManager.resumeBackgroundMusic()
        }
    }
    
    private fun handleGameOver(finalScore: Int) {
        // Save high score
        val sharedPref = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val currentHighScore = sharedPref.getInt("high_score", 0)
        if (finalScore > currentHighScore) {
            sharedPref.edit().putInt("high_score", finalScore).apply()
        }
        
        // Show game over effects
        particleSystem.emit(ParticleSystem.ParticleType.EXPLOSION, 
                          playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 50)
        soundManager.playExplosion()
        
        // Return to main menu after delay
        finish()
    }
    
    // Game event handlers
    fun onPlayerDamaged(damage: Float) {
        playerAvatar.takeDamage(damage)
        gameHUD.showDamageFlash()
        soundManager.playDamage()
        updateHUD()
    }
    
    fun onPlayerHealed(amount: Float) {
        playerAvatar.heal(amount)
        gameHUD.showHealFlash()
        soundManager.playHeal()
        updateHUD()
    }
    
    fun onPlayerLevelUp() {
        gameHUD.showLevelUpEffect()
        soundManager.playLevelUp()
        particleSystem.emit(ParticleSystem.ParticleType.LEVEL_UP, 
                          playerAvatar.x, playerAvatar.y + 1f, playerAvatar.z, 30)
        updateHUD()
    }
    
    fun onCoinCollected() {
        soundManager.playCoinCollect()
        updateHUD()
    }
    
    fun onEnemyDefeated() {
        soundManager.playEnemyHit()
        updateHUD()
    }
    
    fun onExplosion(x: Float, y: Float, z: Float) {
        soundManager.playExplosion()
    }
    
    // Control mode switching
    fun setAccelerometerEnabled(enabled: Boolean) {
        useAccelerometer = enabled
        if (!enabled) {
            inputX = 0f
            inputZ = 0f
        }
    }
    
    fun setTouchControlsEnabled(enabled: Boolean) {
        useTouchControls = enabled
        gameControls.setJoystickEnabled(enabled)
        if (!enabled) {
            inputX = 0f
            inputZ = 0f
        }
    }
    
    // Settings methods
    fun setSoundEnabled(enabled: Boolean) {
        soundManager.setEnabled(enabled)
    }
    
    fun setMasterVolume(volume: Float) {
        soundManager.setMasterVolume(volume)
    }
    
    fun setMusicVolume(volume: Float) {
        soundManager.setMusicVolume(volume)
    }
    
    fun setSfxVolume(volume: Float) {
        soundManager.setSfxVolume(volume)
    }
    
    // Getters for external access
    fun getPlayerAvatar() = playerAvatar
    fun getParticleSystem() = particleSystem
    fun getSoundManager() = soundManager
    fun getGameTime() = gameTime
}
