package com.labyrinth3d.game

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class Bomb : GameObject() {
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: java.nio.ShortBuffer? = null
    private var vertexCount = 0
    private var indexCount = 0
    
    // Bomb properties
    private var size = 0.3f
    private var fuseTime = 5f
    private var currentFuseTime = 5f
    private var explosionRadius = 2f
    private var explosionDamage = 50f
    private var triggered = false
    private var exploded = false
    private var triggerDistance = 1.5f
    
    // Animation properties
    private var pulseTime = 0f
    private var pulseSpeed = 2f
    private var flashTime = 0f
    private var flashSpeed = 8f
    
    // Shader handles
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Bomb colors
    private val bombBodyColor = floatArrayOf(0.3f, 0.3f, 0.3f, 1.0f) // Dark gray
    private val bombFuseColor = floatArrayOf(0.8f, 0.4f, 0.0f, 1.0f) // Orange fuse
    private val bombWarningColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f) // Red warning
    
    init {
        createBombGeometry()
        initializeShaders()
    }
    
    private fun createBombGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        
        // Create bomb body (sphere)
        val segments = 12
        val rings = 8
        
        for (ring in 0..rings) {
            val phi = PI * ring / rings
            val y = cos(phi) * size
            val ringRadius = sin(phi) * size
            
            for (segment in 0..segments) {
                val theta = 2 * PI * segment / segments
                val x = cos(theta) * ringRadius
                val z = sin(theta) * ringRadius
                
                // Add vertex
                vertices.add(x.toFloat())
                vertices.add(y.toFloat())
                vertices.add(z.toFloat())
                
                // Add color (body color)
                colors.addAll(bombBodyColor.toList())
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
        
        // Add fuse (small cylinder on top)
        val fuseHeight = size * 0.5f
        val fuseRadius = size * 0.1f
        val fuseSegments = 6
        val fuseStartIndex = vertices.size / 3
        
        // Fuse bottom
        for (i in 0 until fuseSegments) {
            val angle = 2 * PI * i / fuseSegments
            val x = cos(angle) * fuseRadius
            val z = sin(angle) * fuseRadius
            
            vertices.add(x.toFloat())
            vertices.add(size)
            vertices.add(z.toFloat())
            colors.addAll(bombFuseColor.toList())
        }
        
        // Fuse top
        for (i in 0 until fuseSegments) {
            val angle = 2 * PI * i / fuseSegments
            val x = cos(angle) * fuseRadius
            val z = sin(angle) * fuseRadius
            
            vertices.add(x.toFloat())
            vertices.add(size + fuseHeight)
            vertices.add(z.toFloat())
            colors.addAll(bombFuseColor.toList())
        }
        
        // Fuse indices
        for (i in 0 until fuseSegments) {
            val bottom1 = fuseStartIndex + i
            val bottom2 = fuseStartIndex + (i + 1) % fuseSegments
            val top1 = fuseStartIndex + fuseSegments + i
            val top2 = fuseStartIndex + fuseSegments + (i + 1) % fuseSegments
            
            // Side face
            indices.add(bottom1.toShort())
            indices.add(top1.toShort())
            indices.add(bottom2.toShort())
            
            indices.add(bottom2.toShort())
            indices.add(top1.toShort())
            indices.add(top2.toShort())
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
    
    fun update(deltaTime: Float, playerX: Float, playerY: Float, playerZ: Float) {
        if (exploded) return
        
        // Check if player is close enough to trigger
        val distance = sqrt((x - playerX) * (x - playerX) + (y - playerY) * (y - playerY) + (z - playerZ) * (z - playerZ))
        
        if (!triggered && distance < triggerDistance) {
            triggered = true
        }
        
        if (triggered) {
            currentFuseTime -= deltaTime
            
            // Update animation
            pulseTime += deltaTime * pulseSpeed
            flashTime += deltaTime * flashSpeed
            
            // Flash faster as time runs out
            val timeRatio = currentFuseTime / fuseTime
            flashSpeed = 8f + (1f - timeRatio) * 12f
            
            if (currentFuseTime <= 0) {
                explode()
            }
        }
    }
    
    private fun explode() {
        exploded = true
        // Explosion logic would be handled by game engine
    }
    
    fun draw(mvpMatrix: FloatArray) {
        if (exploded || vertexBuffer == null || colorBuffer == null || indexBuffer == null) return
        
        GLES20.glUseProgram(program)
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set vertex data
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        
        // Modify colors if triggered (flashing effect)
        if (triggered) {
            val flashIntensity = (sin(flashTime) + 1f) / 2f
            val pulseScale = 1f + sin(pulseTime) * 0.1f
            
            // Create flashing color buffer
            val flashColors = mutableListOf<Float>()
            for (i in 0 until vertexCount) {
                if (flashIntensity > 0.5f) {
                    flashColors.addAll(bombWarningColor.toList())
                } else {
                    flashColors.addAll(bombBodyColor.toList())
                }
            }
            
            val flashColorArray = flashColors.toFloatArray()
            val flashColorBuffer = ByteBuffer.allocateDirect(flashColorArray.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(flashColorArray)
            flashColorBuffer.position(0)
            
            GLES20.glVertexAttribPointer(
                colorHandle, 4, GLES20.GL_FLOAT, false, 0, flashColorBuffer
            )
        } else {
            GLES20.glVertexAttribPointer(
                colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer
            )
        }
        
        // Set transformation matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        // Draw the bomb
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    fun checkCollision(otherX: Float, otherY: Float, otherZ: Float, otherRadius: Float): Boolean {
        if (exploded) return false
        
        val dx = x - otherX
        val dy = y - otherY
        val dz = z - otherZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        
        return distance < (size + otherRadius)
    }
    
    fun checkExplosionDamage(otherX: Float, otherY: Float, otherZ: Float): Float {
        if (!exploded) return 0f
        
        val dx = x - otherX
        val dy = y - otherY
        val dz = z - otherZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        
        return if (distance < explosionRadius) {
            val damageRatio = 1f - (distance / explosionRadius)
            explosionDamage * damageRatio
        } else {
            0f
        }
    }
    
    fun isTriggered() = triggered
    fun isExploded() = exploded
    fun getFuseTimeRemaining() = currentFuseTime
    fun getExplosionRadius() = explosionRadius
    fun getSize() = size
}

