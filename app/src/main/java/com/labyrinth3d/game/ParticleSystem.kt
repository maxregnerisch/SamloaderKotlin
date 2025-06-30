package com.labyrinth3d.game

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*
import kotlin.random.Random

class ParticleSystem {
    private val maxParticles = 1000
    private val particles = Array(maxParticles) { Particle() }
    private var activeParticles = 0
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var program = 0
    
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uPointSize;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            gl_PointSize = uPointSize;
            fColor = vColor;
        }
    """.trimIndent()
    
    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;
        
        void main() {
            // Create circular particles
            vec2 coord = gl_PointCoord - vec2(0.5);
            float distance = length(coord);
            if (distance > 0.5) {
                discard;
            }
            
            // Add glow effect
            float alpha = fColor.a * (1.0 - distance * 2.0);
            gl_FragColor = vec4(fColor.rgb, alpha);
        }
    """.trimIndent()
    
    data class Particle(
        var x: Float = 0f,
        var y: Float = 0f,
        var z: Float = 0f,
        var velocityX: Float = 0f,
        var velocityY: Float = 0f,
        var velocityZ: Float = 0f,
        var life: Float = 0f,
        var maxLife: Float = 1f,
        var size: Float = 1f,
        var r: Float = 1f,
        var g: Float = 1f,
        var b: Float = 1f,
        var a: Float = 1f,
        var gravity: Float = -5f,
        var active: Boolean = false
    )
    
    enum class ParticleType {
        EXPLOSION, SPARKLE, SMOKE, FIRE, MAGIC, HEAL, DAMAGE, COIN_COLLECT, LEVEL_UP
    }
    
    init {
        initializeShaders()
        initializeBuffers()
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
    
    private fun initializeBuffers() {
        // Initialize with maximum capacity
        val vertices = FloatArray(maxParticles * 3)
        val colors = FloatArray(maxParticles * 4)
        
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        
        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }
    
    fun emit(type: ParticleType, x: Float, y: Float, z: Float, count: Int = 10) {
        when (type) {
            ParticleType.EXPLOSION -> emitExplosion(x, y, z, count)
            ParticleType.SPARKLE -> emitSparkle(x, y, z, count)
            ParticleType.SMOKE -> emitSmoke(x, y, z, count)
            ParticleType.FIRE -> emitFire(x, y, z, count)
            ParticleType.MAGIC -> emitMagic(x, y, z, count)
            ParticleType.HEAL -> emitHeal(x, y, z, count)
            ParticleType.DAMAGE -> emitDamage(x, y, z, count)
            ParticleType.COIN_COLLECT -> emitCoinCollect(x, y, z, count)
            ParticleType.LEVEL_UP -> emitLevelUp(x, y, z, count)
        }
    }
    
    private fun emitExplosion(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            val angle = Random.nextFloat() * 2 * PI
            val speed = Random.nextFloat() * 8f + 2f
            val elevation = Random.nextFloat() * PI - PI/2
            
            particle.apply {
                this.x = x
                this.y = y
                this.z = z
                velocityX = (cos(angle) * cos(elevation) * speed).toFloat()
                velocityY = (sin(elevation) * speed).toFloat()
                velocityZ = (sin(angle) * cos(elevation) * speed).toFloat()
                life = 1f
                maxLife = Random.nextFloat() * 2f + 1f
                size = Random.nextFloat() * 15f + 5f
                r = 1f
                g = Random.nextFloat() * 0.5f
                b = 0f
                a = 1f
                gravity = -3f
                active = true
            }
        }
    }
    
    private fun emitSparkle(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            val angle = Random.nextFloat() * 2 * PI
            val speed = Random.nextFloat() * 3f + 1f
            
            particle.apply {
                this.x = x + Random.nextFloat() * 0.5f - 0.25f
                this.y = y + Random.nextFloat() * 0.5f
                this.z = z + Random.nextFloat() * 0.5f - 0.25f
                velocityX = (cos(angle) * speed).toFloat()
                velocityY = Random.nextFloat() * 2f + 1f
                velocityZ = (sin(angle) * speed).toFloat()
                life = 1f
                maxLife = Random.nextFloat() * 1.5f + 0.5f
                size = Random.nextFloat() * 8f + 3f
                r = 1f
                g = 1f
                b = Random.nextFloat()
                a = 1f
                gravity = -1f
                active = true
            }
        }
    }
    
    private fun emitSmoke(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            particle.apply {
                this.x = x + Random.nextFloat() * 0.3f - 0.15f
                this.y = y
                this.z = z + Random.nextFloat() * 0.3f - 0.15f
                velocityX = Random.nextFloat() * 1f - 0.5f
                velocityY = Random.nextFloat() * 2f + 1f
                velocityZ = Random.nextFloat() * 1f - 0.5f
                life = 1f
                maxLife = Random.nextFloat() * 3f + 2f
                size = Random.nextFloat() * 20f + 10f
                r = 0.3f
                g = 0.3f
                b = 0.3f
                a = 0.6f
                gravity = 0f
                active = true
            }
        }
    }
    
    private fun emitFire(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            particle.apply {
                this.x = x + Random.nextFloat() * 0.2f - 0.1f
                this.y = y
                this.z = z + Random.nextFloat() * 0.2f - 0.1f
                velocityX = Random.nextFloat() * 0.5f - 0.25f
                velocityY = Random.nextFloat() * 3f + 2f
                velocityZ = Random.nextFloat() * 0.5f - 0.25f
                life = 1f
                maxLife = Random.nextFloat() * 1f + 0.5f
                size = Random.nextFloat() * 12f + 5f
                r = 1f
                g = Random.nextFloat() * 0.5f + 0.5f
                b = Random.nextFloat() * 0.3f
                a = 0.8f
                gravity = 0.5f
                active = true
            }
        }
    }
    
    private fun emitMagic(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            val angle = Random.nextFloat() * 2 * PI
            val radius = Random.nextFloat() * 1f
            val height = Random.nextFloat() * 2f
            
            particle.apply {
                this.x = x + (cos(angle) * radius).toFloat()
                this.y = y + height
                this.z = z + (sin(angle) * radius).toFloat()
                velocityX = (-cos(angle) * 0.5f).toFloat()
                velocityY = Random.nextFloat() * 1f - 0.5f
                velocityZ = (-sin(angle) * 0.5f).toFloat()
                life = 1f
                maxLife = Random.nextFloat() * 2f + 1f
                size = Random.nextFloat() * 10f + 5f
                r = Random.nextFloat() * 0.5f + 0.5f
                g = Random.nextFloat() * 0.3f + 0.7f
                b = 1f
                a = 0.9f
                gravity = 0f
                active = true
            }
        }
    }
    
    private fun emitHeal(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            particle.apply {
                this.x = x + Random.nextFloat() * 0.5f - 0.25f
                this.y = y + Random.nextFloat() * 0.5f
                this.z = z + Random.nextFloat() * 0.5f - 0.25f
                velocityX = Random.nextFloat() * 0.5f - 0.25f
                velocityY = Random.nextFloat() * 2f + 1f
                velocityZ = Random.nextFloat() * 0.5f - 0.25f
                life = 1f
                maxLife = Random.nextFloat() * 2f + 1f
                size = Random.nextFloat() * 8f + 4f
                r = 0.2f
                g = 1f
                b = 0.2f
                a = 0.8f
                gravity = -0.5f
                active = true
            }
        }
    }
    
    private fun emitDamage(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            particle.apply {
                this.x = x + Random.nextFloat() * 0.3f - 0.15f
                this.y = y + Random.nextFloat() * 0.5f + 0.5f
                this.z = z + Random.nextFloat() * 0.3f - 0.15f
                velocityX = Random.nextFloat() * 2f - 1f
                velocityY = Random.nextFloat() * 1f + 0.5f
                velocityZ = Random.nextFloat() * 2f - 1f
                life = 1f
                maxLife = Random.nextFloat() * 1f + 0.5f
                size = Random.nextFloat() * 6f + 3f
                r = 1f
                g = 0.1f
                b = 0.1f
                a = 1f
                gravity = -2f
                active = true
            }
        }
    }
    
    private fun emitCoinCollect(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            val angle = Random.nextFloat() * 2 * PI
            val speed = Random.nextFloat() * 2f + 1f
            
            particle.apply {
                this.x = x
                this.y = y
                this.z = z
                velocityX = (cos(angle) * speed).toFloat()
                velocityY = Random.nextFloat() * 3f + 2f
                velocityZ = (sin(angle) * speed).toFloat()
                life = 1f
                maxLife = Random.nextFloat() * 1.5f + 1f
                size = Random.nextFloat() * 10f + 5f
                r = 1f
                g = 0.8f
                b = 0f
                a = 1f
                gravity = -1f
                active = true
            }
        }
    }
    
    private fun emitLevelUp(x: Float, y: Float, z: Float, count: Int) {
        repeat(count) {
            val particle = getNextParticle() ?: return@repeat
            
            val angle = Random.nextFloat() * 2 * PI
            val radius = Random.nextFloat() * 2f
            val speed = Random.nextFloat() * 4f + 2f
            
            particle.apply {
                this.x = x + (cos(angle) * radius).toFloat()
                this.y = y
                this.z = z + (sin(angle) * radius).toFloat()
                velocityX = (cos(angle) * speed).toFloat()
                velocityY = Random.nextFloat() * 5f + 3f
                velocityZ = (sin(angle) * speed).toFloat()
                life = 1f
                maxLife = Random.nextFloat() * 3f + 2f
                size = Random.nextFloat() * 15f + 10f
                r = Random.nextFloat()
                g = Random.nextFloat()
                b = Random.nextFloat()
                a = 1f
                gravity = -0.5f
                active = true
            }
        }
    }
    
    private fun getNextParticle(): Particle? {
        for (i in 0 until maxParticles) {
            if (!particles[i].active) {
                activeParticles++
                return particles[i]
            }
        }
        return null
    }
    
    fun update(deltaTime: Float) {
        activeParticles = 0
        
        for (particle in particles) {
            if (!particle.active) continue
            
            activeParticles++
            
            // Update position
            particle.x += particle.velocityX * deltaTime
            particle.y += particle.velocityY * deltaTime
            particle.z += particle.velocityZ * deltaTime
            
            // Apply gravity
            particle.velocityY += particle.gravity * deltaTime
            
            // Update life
            particle.life -= deltaTime / particle.maxLife
            
            // Update alpha based on life
            particle.a = particle.life.coerceIn(0f, 1f)
            
            // Deactivate dead particles
            if (particle.life <= 0f) {
                particle.active = false
                activeParticles--
            }
        }
        
        updateBuffers()
    }
    
    private fun updateBuffers() {
        val vertices = FloatArray(activeParticles * 3)
        val colors = FloatArray(activeParticles * 4)
        
        var index = 0
        for (particle in particles) {
            if (!particle.active) continue
            
            // Vertex data
            vertices[index * 3] = particle.x
            vertices[index * 3 + 1] = particle.y
            vertices[index * 3 + 2] = particle.z
            
            // Color data
            colors[index * 4] = particle.r
            colors[index * 4 + 1] = particle.g
            colors[index * 4 + 2] = particle.b
            colors[index * 4 + 3] = particle.a
            
            index++
        }
        
        vertexBuffer?.clear()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)
        
        colorBuffer?.clear()
        colorBuffer?.put(colors)
        colorBuffer?.position(0)
    }
    
    fun render(mvpMatrix: FloatArray) {
        if (activeParticles == 0) return
        
        GLES20.glUseProgram(program)
        
        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Get shader handles
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val pointSizeHandle = GLES20.glGetUniformLocation(program, "uPointSize")
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set vertex data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        
        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(pointSizeHandle, 20f)
        
        // Draw particles
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, activeParticles)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        
        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND)
    }
    
    fun getActiveParticleCount() = activeParticles
    
    fun clear() {
        for (particle in particles) {
            particle.active = false
        }
        activeParticles = 0
    }
}
