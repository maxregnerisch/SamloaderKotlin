package com.labyrinth3d.game

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class Coin : GameObject() {
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: java.nio.ShortBuffer? = null
    private var vertexCount = 0
    private var indexCount = 0
    
    // Coin properties
    private var radius = 0.15f
    private var thickness = 0.05f
    private var segments = 12
    private var rotationAngle = 0f
    private var rotationSpeed = 2f
    private var bobOffset = 0f
    private var bobSpeed = 3f
    private var collected = false
    
    // Shader handles
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Coin colors (golden)
    private val coinColor = floatArrayOf(1.0f, 0.8f, 0.0f, 1.0f)
    private val coinEdgeColor = floatArrayOf(0.8f, 0.6f, 0.0f, 1.0f)
    
    init {
        createCoinGeometry()
        initializeShaders()
    }
    
    private fun createCoinGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        
        var vertexIndex = 0
        
        // Create top face
        vertices.add(0f) // Center top
        vertices.add(thickness / 2)
        vertices.add(0f)
        colors.addAll(coinColor.toList())
        vertexIndex++
        
        for (i in 0..segments) {
            val angle = 2 * PI * i / segments
            val x = cos(angle) * radius
            val z = sin(angle) * radius
            
            vertices.add(x.toFloat())
            vertices.add(thickness / 2)
            vertices.add(z.toFloat())
            colors.addAll(coinColor.toList())
            
            if (i < segments) {
                indices.add(0)
                indices.add((vertexIndex + 1).toShort())
                indices.add((vertexIndex + 2).toShort())
            }
            vertexIndex++
        }
        
        // Create bottom face
        val bottomCenterIndex = vertexIndex
        vertices.add(0f) // Center bottom
        vertices.add(-thickness / 2)
        vertices.add(0f)
        colors.addAll(coinColor.toList())
        vertexIndex++
        
        for (i in 0..segments) {
            val angle = 2 * PI * i / segments
            val x = cos(angle) * radius
            val z = sin(angle) * radius
            
            vertices.add(x.toFloat())
            vertices.add(-thickness / 2)
            vertices.add(z.toFloat())
            colors.addAll(coinColor.toList())
            
            if (i < segments) {
                indices.add(bottomCenterIndex.toShort())
                indices.add((vertexIndex + 2).toShort())
                indices.add((vertexIndex + 1).toShort())
            }
            vertexIndex++
        }
        
        // Create side faces
        val topStartIndex = 1
        val bottomStartIndex = bottomCenterIndex + 1
        
        for (i in 0 until segments) {
            val topCurrent = topStartIndex + i
            val topNext = topStartIndex + (i + 1) % segments
            val bottomCurrent = bottomStartIndex + i
            val bottomNext = bottomStartIndex + (i + 1) % segments
            
            // First triangle
            indices.add(topCurrent.toShort())
            indices.add(bottomCurrent.toShort())
            indices.add(topNext.toShort())
            
            // Second triangle
            indices.add(topNext.toShort())
            indices.add(bottomCurrent.toShort())
            indices.add(bottomNext.toShort())
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
        if (collected) return
        
        // Rotate the coin
        rotationAngle += rotationSpeed * deltaTime
        if (rotationAngle > 360f) rotationAngle -= 360f
        
        // Bob up and down
        bobOffset += bobSpeed * deltaTime
        val bobHeight = sin(bobOffset) * 0.1f
        y = 0.2f + bobHeight
    }
    
    fun draw(mvpMatrix: FloatArray) {
        if (collected || vertexBuffer == null || colorBuffer == null || indexBuffer == null) return
        
        GLES20.glUseProgram(program)
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set vertex data
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer
        )
        
        // Set transformation matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw the coin
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    fun collect() {
        collected = true
    }
    
    fun isCollected() = collected
    
    fun getRadius() = radius
    
    fun checkCollision(otherX: Float, otherY: Float, otherZ: Float, otherRadius: Float): Boolean {
        if (collected) return false
        
        val dx = x - otherX
        val dy = y - otherY
        val dz = z - otherZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        
        return distance < (radius + otherRadius)
    }
}

