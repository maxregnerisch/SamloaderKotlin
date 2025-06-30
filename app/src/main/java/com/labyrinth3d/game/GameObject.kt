package com.labyrinth3d.game

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class GameObject {
    protected var x = 0f
    protected var y = 0f
    protected var z = 0f
    protected var active = true
    protected var rotation = 0f
    
    protected var vertexBuffer: FloatBuffer? = null
    protected var colorBuffer: FloatBuffer? = null
    protected var program = 0
    
    protected val modelMatrix = FloatArray(16)
    protected val mvpMatrix = FloatArray(16)
    
    // Shader code
    protected val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fColor = vColor;
        }
    """.trimIndent()
    
    protected val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;
        void main() {
            gl_FragColor = fColor;
        }
    """.trimIndent()
    
    init {
        initializeBuffers()
        initializeShaders()
    }
    
    abstract fun initializeBuffers()
    
    private fun initializeShaders() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    
    open fun update() {
        rotation += 2f
        if (rotation >= 360f) rotation = 0f
    }
    
    open fun draw(mvpMatrix: FloatArray) {
        if (!active) return
        
        // Set up model matrix
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.rotateM(modelMatrix, 0, rotation, 0f, 1f, 0f)
        
        // Combine with MVP matrix
        Matrix.multiplyMM(this.mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        
        // Use shader program
        GLES20.glUseProgram(program)
        
        // Get handles
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Prepare coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        
        // Apply MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, this.mvpMatrix, 0)
        
        // Draw
        drawGeometry()
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    abstract fun drawGeometry()
    
    fun setPosition(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }
    
    fun setActive(active: Boolean) {
        this.active = active
    }
    
    fun isActive() = active
    
    fun checkCollision(otherX: Float, otherZ: Float, radius: Float): Boolean {
        val distance = sqrt((x - otherX) * (x - otherX) + (z - otherZ) * (z - otherZ))
        return distance < radius + 0.5f // 0.5f is this object's collision radius
    }
    
    protected fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val floatBuffer = buffer.asFloatBuffer()
        floatBuffer.put(data)
        floatBuffer.position(0)
        return floatBuffer
    }
}

class Ball : GameObject() {
    override fun initializeBuffers() {
        // Create sphere vertices (simplified icosphere)
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        
        val radius = 0.3f
        val stacks = 10
        val slices = 10
        
        for (i in 0..stacks) {
            val phi = Math.PI * i / stacks
            for (j in 0..slices) {
                val theta = 2 * Math.PI * j / slices
                
                val x = (radius * sin(phi) * cos(theta)).toFloat()
                val y = (radius * cos(phi)).toFloat()
                val z = (radius * sin(phi) * sin(theta)).toFloat()
                
                vertices.addAll(listOf(x, y, z))
                colors.addAll(listOf(1f, 0.8f, 0.2f, 1f)) // Golden color
            }
        }
        
        vertexBuffer = createFloatBuffer(vertices.toFloatArray())
        colorBuffer = createFloatBuffer(colors.toFloatArray())
    }
    
    override fun drawGeometry() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexBuffer!!.capacity() / 3)
    }
}

class Coin : GameObject() {
    override fun initializeBuffers() {
        val vertices = floatArrayOf(
            // Coin shape (octagon)
            0f, 0f, 0f,
            0.2f, 0f, 0f,
            0.14f, 0f, 0.14f,
            0f, 0f, 0.2f,
            -0.14f, 0f, 0.14f,
            -0.2f, 0f, 0f,
            -0.14f, 0f, -0.14f,
            0f, 0f, -0.2f,
            0.14f, 0f, -0.14f
        )
        
        val colors = floatArrayOf(
            1f, 1f, 0f, 1f, // Yellow center
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f
        )
        
        vertexBuffer = createFloatBuffer(vertices)
        colorBuffer = createFloatBuffer(colors)
    }
    
    override fun drawGeometry() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 9)
    }
}

class Enemy : GameObject() {
    private var moveDirection = 1f
    private var moveSpeed = 0.02f
    
    override fun initializeBuffers() {
        val vertices = floatArrayOf(
            // Enemy shape (diamond)
            0f, 0.3f, 0f,    // Top
            -0.2f, 0f, 0.2f, // Front left
            0.2f, 0f, 0.2f,  // Front right
            0.2f, 0f, -0.2f, // Back right
            -0.2f, 0f, -0.2f, // Back left
            0f, -0.3f, 0f    // Bottom
        )
        
        val colors = floatArrayOf(
            1f, 0f, 0f, 1f, // Red
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f
        )
        
        vertexBuffer = createFloatBuffer(vertices)
        colorBuffer = createFloatBuffer(colors)
    }
    
    override fun update() {
        super.update()
        // Simple AI movement
        x += moveDirection * moveSpeed
        if (x > 8f || x < -8f) {
            moveDirection *= -1f
        }
    }
    
    override fun drawGeometry() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
    }
}

class Bomb : GameObject() {
    override fun initializeBuffers() {
        val vertices = floatArrayOf(
            // Bomb shape (cube)
            -0.15f, -0.15f, 0.15f,
            0.15f, -0.15f, 0.15f,
            0.15f, 0.15f, 0.15f,
            -0.15f, 0.15f, 0.15f,
            -0.15f, -0.15f, -0.15f,
            0.15f, -0.15f, -0.15f,
            0.15f, 0.15f, -0.15f,
            -0.15f, 0.15f, -0.15f
        )
        
        val colors = floatArrayOf(
            0.2f, 0.2f, 0.2f, 1f, // Dark gray
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f,
            0.2f, 0.2f, 0.2f, 1f
        )
        
        vertexBuffer = createFloatBuffer(vertices)
        colorBuffer = createFloatBuffer(colors)
    }
    
    override fun drawGeometry() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 8)
    }
}

class MathQuestion : GameObject() {
    val question: String
    val answer: Int
    
    init {
        val num1 = (1..10).random()
        val num2 = (1..10).random()
        val operation = listOf("+", "-", "*").random()
        
        when (operation) {
            "+" -> {
                question = "$num1 + $num2 = ?"
                answer = num1 + num2
            }
            "-" -> {
                question = "$num1 - $num2 = ?"
                answer = num1 - num2
            }
            "*" -> {
                question = "$num1 × $num2 = ?"
                answer = num1 * num2
            }
            else -> {
                question = "1 + 1 = ?"
                answer = 2
            }
        }
    }
    
    override fun initializeBuffers() {
        val vertices = floatArrayOf(
            // Question mark shape (simplified)
            -0.1f, 0.3f, 0f,
            0.1f, 0.3f, 0f,
            0.1f, 0.1f, 0f,
            0f, 0f, 0f,
            0f, -0.2f, 0f
        )
        
        val colors = floatArrayOf(
            0f, 1f, 1f, 1f, // Cyan
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0f, 1f, 1f, 1f
        )
        
        vertexBuffer = createFloatBuffer(vertices)
        colorBuffer = createFloatBuffer(colors)
    }
    
    override fun drawGeometry() {
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, 5)
    }
}

