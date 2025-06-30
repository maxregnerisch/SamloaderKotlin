package com.labyrinth3d.game

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {
    
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    
    private var gameEngine: GameEngine? = null
    private var labyrinth: Labyrinth? = null
    private var ball: Ball? = null
    private var gameObjects: MutableList<GameObject> = mutableListOf()
    private var playerAvatar: PlayerAvatar? = null
    private var particleSystem: ParticleSystem? = null
    
    fun setGameEngine(engine: GameEngine) {
        this.gameEngine = engine
    }
    
    fun setPlayerAvatar(avatar: PlayerAvatar) {
        this.playerAvatar = avatar
    }
    
    fun setParticleSystem(system: ParticleSystem) {
        this.particleSystem = system
    }
    
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.2f, 1.0f)
        
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        
        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Initialize game objects
        initializeGameObjects()
    }
    
    private fun initializeGameObjects() {
        try {
            labyrinth = Labyrinth()
            ball = Ball()
            
            // Initialize coins, enemies, and bombs
            gameObjects.clear()
            
            // Add coins
            for (i in 0 until 10) {
                try {
                    val coin = Coin()
                    coin.setPosition(
                        (Math.random() * 18 - 9).toFloat(),
                        0.2f,
                        (Math.random() * 18 - 9).toFloat()
                    )
                    gameObjects.add(coin)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Add enemies
            for (i in 0 until 3) {
                try {
                    val enemy = Enemy()
                    enemy.setPosition(
                        (Math.random() * 16 - 8).toFloat(),
                        0.3f,
                        (Math.random() * 16 - 8).toFloat()
                    )
                    gameObjects.add(enemy)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Add bombs
            for (i in 0 until 5) {
                try {
                    val bomb = Bomb()
                    bomb.setPosition(
                        (Math.random() * 16 - 8).toFloat(),
                        0.2f,
                        (Math.random() * 16 - 8).toFloat()
                    )
                    gameObjects.add(bomb)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Add math questions
            for (i in 0 until 3) {
                try {
                    val mathQuestion = MathQuestion()
                    mathQuestion.setPosition(
                        (Math.random() * 16 - 8).toFloat(),
                        0.4f,
                        (Math.random() * 16 - 8).toFloat()
                    )
                    gameObjects.add(mathQuestion)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDrawFrame(unused: GL10) {
        try {
            // Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            
            // Update game logic
            try {
                gameEngine?.update()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Set up camera
            try {
                setupCamera()
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            
            // Draw labyrinth
            try {
                labyrinth?.draw(mvpMatrix)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Draw ball
            try {
                ball?.let { ballObj ->
                    gameEngine?.let { engine ->
                        ballObj.setPosition(engine.getBallX(), 0.5f, engine.getBallZ())
                    }
                    ballObj.draw(mvpMatrix)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Draw game objects
            try {
                for (gameObject in gameObjects) {
                    try {
                        gameObject.update()
                        gameObject.draw(mvpMatrix)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Draw player avatar
            try {
                playerAvatar?.let { avatar ->
                    avatar.draw(mvpMatrix)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Update and draw particle system
            try {
                particleSystem?.let { particles ->
                    particles.update(1f / 60f) // Approximate frame time
                    particles.render(mvpMatrix)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Check collisions
            try {
                checkCollisions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupCamera() {
        // Set the camera position (View matrix)
        val playerX = playerAvatar?.x ?: 0f
        val playerZ = playerAvatar?.z ?: 0f
        
        Matrix.setLookAtM(
            viewMatrix, 0,
            playerX, 8f, playerZ + 5f,  // Camera position (following player)
            playerX, 0f, playerZ,       // Look at player
            0f, 1.0f, 0.0f              // Up vector
        )
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }
    
    private fun checkCollisions() {
        val playerX = playerAvatar?.x ?: 0f
        val playerZ = playerAvatar?.z ?: 0f
        val playerRadius = 0.3f
        
        for (gameObject in gameObjects) {
            try {
                if (gameObject.isActive() && gameObject.checkCollision(playerX, playerZ, playerRadius)) {
                    when (gameObject) {
                        is Coin -> {
                            gameEngine?.addScore(10)
                            gameObject.active = false
                            gameEngine?.onCoinCollected()
                        }
                        is Enemy -> {
                            gameEngine?.onPlayerDamaged(25f)
                            gameObject.active = false
                        }
                        is Bomb -> {
                            gameEngine?.onPlayerDamaged(50f)
                            gameEngine?.onExplosion(gameObject.x, gameObject.y, gameObject.z)
                            gameObject.active = false
                        }
                        is MathQuestion -> {
                            gameEngine?.showMathQuestion(gameObject)
                            gameObject.active = false
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        val ratio: Float = width.toFloat() / height.toFloat()
        
        // This projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)
    }
}
