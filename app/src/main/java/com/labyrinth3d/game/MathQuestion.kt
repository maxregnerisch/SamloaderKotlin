package com.labyrinth3d.game

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*
import kotlin.random.Random

class MathQuestion : GameObject() {
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: java.nio.ShortBuffer? = null
    private var vertexCount = 0
    private var indexCount = 0
    
    // Math question properties
    private var size = 0.5f
    private var question = ""
    private var correctAnswer = 0
    private var options = mutableListOf<Int>()
    private var answered = false
    private var correctlyAnswered = false
    private var difficulty = 1
    private var reward = 100
    
    // Animation properties
    private var rotationAngle = 0f
    private var rotationSpeed = 1f
    private var bobOffset = 0f
    private var bobSpeed = 2f
    private var glowIntensity = 0f
    private var glowSpeed = 3f
    
    // Shader handles
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Question colors
    private val questionColor = floatArrayOf(0.2f, 0.8f, 0.2f, 1.0f) // Green
    private val questionGlowColor = floatArrayOf(0.4f, 1.0f, 0.4f, 1.0f) // Bright green
    private val questionSymbolColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f) // White
    
    init {
        generateQuestion()
        createQuestionGeometry()
        initializeShaders()
    }
    
    private fun generateQuestion() {
        when (difficulty) {
            1 -> generateBasicQuestion()
            2 -> generateIntermediateQuestion()
            3 -> generateAdvancedQuestion()
            else -> generateBasicQuestion()
        }
        
        generateOptions()
    }
    
    private fun generateBasicQuestion() {
        val operation = Random.nextInt(4) // 0: +, 1: -, 2: *, 3: /
        val num1 = Random.nextInt(10) + 1
        val num2 = Random.nextInt(10) + 1
        
        when (operation) {
            0 -> {
                question = "$num1 + $num2 = ?"
                correctAnswer = num1 + num2
            }
            1 -> {
                val larger = maxOf(num1, num2)
                val smaller = minOf(num1, num2)
                question = "$larger - $smaller = ?"
                correctAnswer = larger - smaller
            }
            2 -> {
                val smallNum1 = Random.nextInt(5) + 1
                val smallNum2 = Random.nextInt(5) + 1
                question = "$smallNum1 × $smallNum2 = ?"
                correctAnswer = smallNum1 * smallNum2
            }
            3 -> {
                val divisor = Random.nextInt(5) + 2
                val dividend = divisor * (Random.nextInt(5) + 1)
                question = "$dividend ÷ $divisor = ?"
                correctAnswer = dividend / divisor
            }
        }
        reward = 50
    }
    
    private fun generateIntermediateQuestion() {
        val operation = Random.nextInt(4)
        val num1 = Random.nextInt(50) + 10
        val num2 = Random.nextInt(20) + 5
        
        when (operation) {
            0 -> {
                question = "$num1 + $num2 = ?"
                correctAnswer = num1 + num2
            }
            1 -> {
                question = "$num1 - $num2 = ?"
                correctAnswer = num1 - num2
            }
            2 -> {
                val smallNum1 = Random.nextInt(12) + 3
                val smallNum2 = Random.nextInt(8) + 2
                question = "$smallNum1 × $smallNum2 = ?"
                correctAnswer = smallNum1 * smallNum2
            }
            3 -> {
                val divisor = Random.nextInt(8) + 3
                val dividend = divisor * (Random.nextInt(10) + 2)
                question = "$dividend ÷ $divisor = ?"
                correctAnswer = dividend / divisor
            }
        }
        reward = 100
    }
    
    private fun generateAdvancedQuestion() {
        val questionType = Random.nextInt(3)
        
        when (questionType) {
            0 -> {
                // Square roots
                val perfectSquares = listOf(4, 9, 16, 25, 36, 49, 64, 81, 100)
                val square = perfectSquares[Random.nextInt(perfectSquares.size)]
                question = "√$square = ?"
                correctAnswer = sqrt(square.toDouble()).toInt()
            }
            1 -> {
                // Powers
                val base = Random.nextInt(5) + 2
                val exponent = Random.nextInt(3) + 2
                question = "$base^$exponent = ?"
                correctAnswer = base.toDouble().pow(exponent.toDouble()).toInt()
            }
            2 -> {
                // Complex arithmetic
                val num1 = Random.nextInt(20) + 10
                val num2 = Random.nextInt(10) + 5
                val num3 = Random.nextInt(5) + 2
                question = "($num1 + $num2) × $num3 = ?"
                correctAnswer = (num1 + num2) * num3
            }
        }
        reward = 200
    }
    
    private fun generateOptions() {
        options.clear()
        options.add(correctAnswer)
        
        // Add 3 wrong answers
        for (i in 0 until 3) {
            var wrongAnswer: Int
            do {
                val variation = Random.nextInt(20) - 10
                wrongAnswer = correctAnswer + variation
            } while (wrongAnswer == correctAnswer || options.contains(wrongAnswer) || wrongAnswer < 0)
            
            options.add(wrongAnswer)
        }
        
        options.shuffle()
    }
    
    private fun createQuestionGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        
        // Create a floating question mark symbol (simplified)
        val segments = 16
        
        // Main body (octahedron shape)
        val halfSize = size / 2
        
        // Top pyramid
        vertices.add(0f) // Top vertex
        vertices.add(halfSize)
        vertices.add(0f)
        colors.addAll(questionColor.toList())
        
        // Middle vertices
        for (i in 0 until segments) {
            val angle = 2 * PI * i / segments
            val x = cos(angle) * halfSize
            val z = sin(angle) * halfSize
            
            vertices.add(x.toFloat())
            vertices.add(0f)
            vertices.add(z.toFloat())
            colors.addAll(questionColor.toList())
        }
        
        // Bottom vertex
        vertices.add(0f)
        vertices.add(-halfSize)
        vertices.add(0f)
        colors.addAll(questionColor.toList())
        
        // Top pyramid indices
        for (i in 0 until segments) {
            indices.add(0) // Top vertex
            indices.add((i + 1).toShort())
            indices.add(((i + 1) % segments + 1).toShort())
        }
        
        // Bottom pyramid indices
        val bottomVertex = segments + 1
        for (i in 0 until segments) {
            indices.add(bottomVertex.toShort())
            indices.add(((i + 1) % segments + 1).toShort())
            indices.add((i + 1).toShort())
        }
        
        // Add question mark symbol on top
        val symbolSize = halfSize * 0.3f
        val symbolHeight = halfSize + 0.1f
        
        // Question mark curve (simplified as small cubes)
        val symbolVertices = arrayOf(
            // Question mark top curve
            0f, symbolHeight, symbolSize,
            symbolSize, symbolHeight, symbolSize,
            symbolSize, symbolHeight, 0f,
            0f, symbolHeight, 0f,
            
            // Question mark stem
            symbolSize * 0.5f, symbolHeight - symbolSize * 0.5f, 0f,
            symbolSize * 0.5f, symbolHeight - symbolSize, 0f,
            
            // Question mark dot
            symbolSize * 0.5f, symbolHeight - symbolSize * 1.5f, 0f
        )
        
        val startIndex = vertices.size / 3
        for (i in symbolVertices.indices step 3) {
            vertices.add(symbolVertices[i])
            vertices.add(symbolVertices[i + 1])
            vertices.add(symbolVertices[i + 2])
            colors.addAll(questionSymbolColor.toList())
        }
        
        // Convert to buffers
        val vertexArray = vertices.toFloatArray()
        val colorArray = colors.toFloatArray()
        val indexArray = indices.toShortArray()
        
        vertexCount = vertexArray.size / 3
        indexCount = indexArray.size
        
        vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexArray)
        vertexBuffer?.position(0)
        
        colorBuffer = ByteBuffer.allocateDirect(colorArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(colorArray)
        colorBuffer?.position(0)
        
        indexBuffer = ByteBuffer.allocateDirect(indexArray.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indexArray)
        indexBuffer?.position(0)
    }
    
    private fun initializeShaders() {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute vec4 vColor;
            varying vec4 fColor;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                fColor = vColor;
            }
        """.trimIndent()
        
        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 fColor;
            void main() {
                gl_FragColor = fColor;
            }
        """.trimIndent()
        
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // Get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    
    fun update(deltaTime: Float) {
        if (answered) return
        
        // Rotate the question
        rotationAngle += rotationSpeed * deltaTime
        if (rotationAngle > 360f) rotationAngle -= 360f
        
        // Bob up and down
        bobOffset += bobSpeed * deltaTime
        val bobHeight = sin(bobOffset) * 0.15f
        y = 0.4f + bobHeight
        
        // Glow effect
        glowIntensity += glowSpeed * deltaTime
    }
    
    fun draw(mvpMatrix: FloatArray) {
        if (answered || vertexBuffer == null || colorBuffer == null || indexBuffer == null) return
        
        GLES20.glUseProgram(program)
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set vertex data
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        
        // Apply glow effect
        val glow = (sin(glowIntensity) + 1f) / 2f
        val glowColors = mutableListOf<Float>()
        
        for (i in 0 until vertexCount) {
            val baseColor = if (i < vertexCount - 7) questionColor else questionSymbolColor
            val glowColor = questionGlowColor
            
            for (j in 0 until 4) {
                val color = baseColor[j] + (glowColor[j] - baseColor[j]) * glow * 0.3f
                glowColors.add(color.coerceIn(0f, 1f))
            }
        }
        
        val glowColorArray = glowColors.toFloatArray()
        val glowColorBuffer = ByteBuffer.allocateDirect(glowColorArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(glowColorArray)
        glowColorBuffer.position(0)
        
        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT, false, 0, glowColorBuffer
        )
        
        // Set transformation matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw the question
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    fun checkCollision(otherX: Float, otherY: Float, otherZ: Float, otherRadius: Float): Boolean {
        if (answered) return false
        
        val dx = x - otherX
        val dy = y - otherY
        val dz = z - otherZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        
        return distance < (size + otherRadius)
    }
    
    fun answerQuestion(answer: Int): Boolean {
        if (answered) return false
        
        answered = true
        correctlyAnswered = (answer == correctAnswer)
        return correctlyAnswered
    }
    
    fun getQuestion() = question
    fun getOptions() = options.toList()
    fun getCorrectAnswer() = correctAnswer
    fun isAnswered() = answered
    fun isCorrectlyAnswered() = correctlyAnswered
    fun getReward() = reward
    fun getDifficulty() = difficulty
    fun getSize() = size
    
    fun setDifficulty(newDifficulty: Int) {
        difficulty = newDifficulty.coerceIn(1, 3)
        generateQuestion()
    }
}

