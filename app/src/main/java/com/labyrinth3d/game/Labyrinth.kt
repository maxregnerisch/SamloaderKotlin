package com.labyrinth3d.game

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Labyrinth {
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var program = 0
    
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    
    // Shader code
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fColor = vColor;
        }
    """.trimIndent()
    
    private val fragmentShaderCode = """
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
    
    private fun initializeBuffers() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        
        // Create labyrinth floor
        createFloor(vertices, colors)
        
        // Create outer walls
        createOuterWalls(vertices, colors)
        
        // Create inner maze walls
        createInnerWalls(vertices, colors)
        
        vertexBuffer = createFloatBuffer(vertices.toFloatArray())
        colorBuffer = createFloatBuffer(colors.toFloatArray())
    }
    
    private fun createFloor(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val size = 10f
        val gridSize = 20
        val step = size * 2 / gridSize
        
        // Create a grid floor
        for (i in 0..gridSize) {
            for (j in 0..gridSize) {
                val x = -size + i * step
                val z = -size + j * step
                
                // Floor quad
                vertices.addAll(listOf(
                    x, 0f, z,
                    x + step, 0f, z,
                    x + step, 0f, z + step,
                    x, 0f, z + step
                ))
                
                // Alternating floor colors for checkerboard pattern
                val color = if ((i + j) % 2 == 0) {
                    listOf(0.3f, 0.3f, 0.4f, 1f) // Dark blue-gray
                } else {
                    listOf(0.4f, 0.4f, 0.5f, 1f) // Light blue-gray
                }
                
                repeat(4) { colors.addAll(color) }
            }
        }
    }
    
    private fun createOuterWalls(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val size = 10f
        val height = 2f
        val thickness = 0.5f
        
        // North wall
        createWall(vertices, colors, -size, size, size, size, height, thickness)
        // South wall
        createWall(vertices, colors, -size, -size, size, -size, height, thickness)
        // East wall
        createWall(vertices, colors, size, -size, size, size, height, thickness)
        // West wall
        createWall(vertices, colors, -size, -size, -size, size, height, thickness)
    }
    
    private fun createInnerWalls(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val height = 1.5f
        val thickness = 0.3f
        
        // Define maze walls (x1, z1, x2, z2)
        val walls = listOf(
            floatArrayOf(-5f, -2f, 0f, -2f),
            floatArrayOf(2f, -2f, 7f, -2f),
            floatArrayOf(-7f, 2f, -2f, 2f),
            floatArrayOf(0f, 2f, 5f, 2f),
            floatArrayOf(-3f, 5f, 3f, 5f),
            floatArrayOf(-2f, -7f, -2f, -4f),
            floatArrayOf(2f, -5f, 2f, 0f),
            floatArrayOf(5f, -7f, 5f, -4f),
            floatArrayOf(-5f, 0f, -5f, 4f),
            floatArrayOf(3f, 3f, 3f, 7f)
        )
        
        for (wall in walls) {
            createWall(vertices, colors, wall[0], wall[1], wall[2], wall[3], height, thickness)
        }
    }
    
    private fun createWall(
        vertices: MutableList<Float>, 
        colors: MutableList<Float>,
        x1: Float, z1: Float, x2: Float, z2: Float,
        height: Float, thickness: Float
    ) {
        val centerX = (x1 + x2) / 2
        val centerZ = (z1 + z2) / 2
        val length = kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1))
        val angle = kotlin.math.atan2(z2 - z1, x2 - x1)
        
        val halfLength = length / 2
        val halfThickness = thickness / 2
        
        // Wall vertices (box)
        val wallVertices = floatArrayOf(
            // Front face
            -halfLength, 0f, halfThickness,
            halfLength, 0f, halfThickness,
            halfLength, height, halfThickness,
            -halfLength, height, halfThickness,
            
            // Back face
            -halfLength, 0f, -halfThickness,
            halfLength, 0f, -halfThickness,
            halfLength, height, -halfThickness,
            -halfLength, height, -halfThickness,
            
            // Top face
            -halfLength, height, -halfThickness,
            halfLength, height, -halfThickness,
            halfLength, height, halfThickness,
            -halfLength, height, halfThickness
        )
        
        // Transform vertices
        for (i in wallVertices.indices step 3) {
            val x = wallVertices[i]
            val y = wallVertices[i + 1]
            val z = wallVertices[i + 2]
            
            // Rotate
            val rotatedX = x * kotlin.math.cos(angle) - z * kotlin.math.sin(angle)
            val rotatedZ = x * kotlin.math.sin(angle) + z * kotlin.math.cos(angle)
            
            // Translate
            vertices.addAll(listOf(
                (rotatedX + centerX).toFloat(),
                y,
                (rotatedZ + centerZ).toFloat()
            ))
        }
        
        // Wall colors (stone-like)
        val wallColor = listOf(0.6f, 0.6f, 0.7f, 1f)
        repeat(12) { colors.addAll(wallColor) }
    }
    
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
    
    fun draw(mvpMatrix: FloatArray) {
        // Set up model matrix
        Matrix.setIdentityM(modelMatrix, 0)
        
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
        
        // Draw the labyrinth
        GLES20.glDrawArrays(GLES20.GL_QUADS, 0, vertexBuffer!!.capacity() / 3)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val floatBuffer = buffer.asFloatBuffer()
        floatBuffer.put(data)
        floatBuffer.position(0)
        return floatBuffer
    }
}

