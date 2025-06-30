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
    
    fun setGameEngine(engine: GameEngine) {
        this.gameEngine = engine
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
        labyrinth = Labyrinth()
        ball = Ball()
        
        // Initialize coins, enemies, and bombs
        gameObjects.clear()
        
        // Add coins
        for (i in 0 until 10) {
            val coin = Coin()
            coin.setPosition(
                (Math.random() * 18 - 9).toFloat(),
                0.2f,
                (Math.random() * 18 - 9).toFloat()
            )
            gameObjects.add(coin)
        }
        
        // Add enemies
        for (i in 0 until 3) {
            val enemy = Enemy()
            enemy.setPosition(
                (Math.random() * 16 - 8).toFloat(),
                0.3f,
                (Math.random() * 16 - 8).toFloat()
            )
            gameObjects.add(enemy)
        }
        
        // Add bombs
        for (i in 0 until 5) {
            val bomb = Bomb()
            bomb.setPosition(
                (Math.random() * 16 - 8).toFloat(),
                0.2f,
                (Math.random() * 16 - 8).toFloat()
            )
            gameObjects.add(bomb)
        }
        
        // Add math questions
        for (i in 0 until 3) {
            val mathQuestion = MathQuestion()
            mathQuestion.setPosition(
                (Math.random() * 16 - 8).toFloat(),
                0.4f,
                (Math.random() * 16 - 8).toFloat()
            )
            gameObjects.add(mathQuestion)
        }
    }
    
    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // Update game logic
        gameEngine?.update()
        
        // Set up camera
        setupCamera()
        
        // Draw labyrinth
        labyrinth?.draw(mvpMatrix)
        
        // Draw ball
        ball?.let { ballObj ->
            gameEngine?.let { engine ->
                ballObj.setPosition(engine.getBallX(), 0.5f, engine.getBallZ())
            }
            ballObj.draw(mvpMatrix)
        }
        
        // Draw game objects
        for (gameObject in gameObjects) {
            if (gameObject.isActive()) {
                gameObject.update()
                gameObject.draw(mvpMatrix)
            }
        }
        
        // Check collisions
        checkCollisions()
    }
    
    private fun setupCamera() {
        // Set the camera position (View matrix)
        val ballX = gameEngine?.getBallX() ?: 0f
        val ballZ = gameEngine?.getBallZ() ?: 0f
        
        Matrix.setLookAtM(
            viewMatrix, 0,
            ballX, 8f, ballZ + 5f,  // Camera position (following ball)
            ballX, 0f, ballZ,       // Look at ball
            0f, 1.0f, 0.0f          // Up vector
        )
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }
    
    private fun checkCollisions() {
        val ballX = gameEngine?.getBallX() ?: 0f
        val ballZ = gameEngine?.getBallZ() ?: 0f
        
        for (gameObject in gameObjects) {
            if (gameObject.isActive() && gameObject.checkCollision(ballX, ballZ, 0.5f)) {
                when (gameObject) {
                    is Coin -> {
                        gameEngine?.addScore(10)
                        gameObject.setActive(false)
                        gameEngine?.onVibrate(50)
                    }
                    is Enemy -> {
                        gameEngine?.loseLife()
                        gameObject.setActive(false)
                        gameEngine?.onVibrate(200)
                    }
                    is Bomb -> {
                        gameEngine?.loseLife()
                        gameObject.setActive(false)
                        gameEngine?.onVibrate(300)
                    }
                    is MathQuestion -> {
                        // Handle math question interaction
                        gameEngine?.showMathQuestion(gameObject as MathQuestion)
                        gameObject.setActive(false)
                    }
                }
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

