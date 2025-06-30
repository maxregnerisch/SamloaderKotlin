package com.labyrinth3d.game

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class Ball : GameObject() {
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: java.nio.ShortBuffer? = null
    private var vertexCount = 0
    private var indexCount = 0
    
    // Ball properties
    private var radius = 0.3f
    private var segments = 16
    private var rings = 12
    
    // Shader handles
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Ball color
    private val ballColor = floatArrayOf(1.0f, 0.8f, 0.2f, 1.0f) // Golden ball
    
    init {
        createBallGeometry()
        initializeShaders()
    }
    
    private fun createBallGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        
        // Generate sphere vertices
        for (ring in 0..rings) {
            val phi = PI * ring / rings
            val y = cos(phi) * radius
            val ringRadius = sin(phi) * radius
            
            for (segment in 0..segments) {
                val theta = 2 * PI * segment / segments
                val x = cos(theta) * ringRadius
                val z = sin(theta) * ringRadius
                
                // Add vertex
                vertices.add(x.toFloat())
                vertices.add(y.toFloat())
                vertices.add(z.toFloat())
                
                // Add color
                colors.addAll(ballColor.toList())
            }
        }
        
        // Generate indices for triangles
        for (ring in 0 until rings) {
            for (segment in 0 until segments) {
                val current = (ring * (segments + 1) + segment).toShort()
                val next = (ring * (segments + 1) + segment + 1).toShort()
                val below = ((ring + 1) * (segments + 1) + segment).toShort()
                val belowNext = ((ring + 1) * (segments + 1) + segment + 1).toShort()
                
                // First triangle
                indices.add(current)
                indices.add(below)
                indices.add(next)
                
                // Second triangle
                indices.add(next)
                indices.add(below)
                indices.add(belowNext)
            }
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
    
    fun draw(mvpMatrix: FloatArray) {
        if (vertexBuffer == null || colorBuffer == null || indexBuffer == null) return
        
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
        
        // Draw the ball
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    fun getRadius() = radius
    
    fun setRadius(newRadius: Float) {
        radius = newRadius
        createBallGeometry()
    }
}

