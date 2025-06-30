package com.labyrinth3d.game

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*
import kotlin.random.Random

class Enemy : GameObject() {
    
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var indexBuffer: java.nio.ShortBuffer? = null
    private var vertexCount = 0
    private var indexCount = 0
    
    // Enemy properties
    private var size = 0.4f
    private var speed = 1.5f
    private var health = 100f
    private var maxHealth = 100f
    private var attackDamage = 25f
    private var attackRange = 1.0f
    private var detectionRange = 3.0f
    private var alive = true
    
    // AI properties
    private var targetX = 0f
    private var targetZ = 0f
    private var velocityX = 0f
    private var velocityZ = 0f
    private var lastAttackTime = 0f
    private var attackCooldown = 2f
    private var patrolRadius = 2f
    private var originalX = 0f
    private var originalZ = 0f
    private var aiState = AIState.PATROL
    
    // Animation properties
    private var animationTime = 0f
    private var bobOffset = 0f
    
    // Shader handles
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    // Enemy colors (red/dark)
    private val enemyColor = floatArrayOf(0.8f, 0.2f, 0.2f, 1.0f)
    private val enemyEyeColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    
    enum class AIState {
        PATROL, CHASE, ATTACK, STUNNED
    }
    
    init {
        createEnemyGeometry()
        initializeShaders()
    }
    
    override fun setPosition(newX: Float, newY: Float, newZ: Float) {
        super.setPosition(newX, newY, newZ)
        originalX = newX
        originalZ = newZ
        generatePatrolTarget()
    }
    
    private fun createEnemyGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        
        // Create a simple enemy shape (cube with spikes)
        val halfSize = size / 2
        
        // Main body (cube)
        val cubeVertices = arrayOf(
            // Front face
            -halfSize, -halfSize, halfSize,
            halfSize, -halfSize, halfSize,
            halfSize, halfSize, halfSize,
            -halfSize, halfSize, halfSize,
            
            // Back face
            -halfSize, -halfSize, -halfSize,
            -halfSize, halfSize, -halfSize,
            halfSize, halfSize, -halfSize,
            halfSize, -halfSize, -halfSize,
            
            // Top face
            -halfSize, halfSize, -halfSize,
            -halfSize, halfSize, halfSize,
            halfSize, halfSize, halfSize,
            halfSize, halfSize, -halfSize,
            
            // Bottom face
            -halfSize, -halfSize, -halfSize,
            halfSize, -halfSize, -halfSize,
            halfSize, -halfSize, halfSize,
            -halfSize, -halfSize, halfSize,
            
            // Right face
            halfSize, -halfSize, -halfSize,
            halfSize, halfSize, -halfSize,
            halfSize, halfSize, halfSize,
            halfSize, -halfSize, halfSize,
            
            // Left face
            -halfSize, -halfSize, -halfSize,
            -halfSize, -halfSize, halfSize,
            -halfSize, halfSize, halfSize,
            -halfSize, halfSize, -halfSize
        )
        
        // Add vertices
        for (i in cubeVertices.indices step 3) {
            vertices.add(cubeVertices[i])
            vertices.add(cubeVertices[i + 1])
            vertices.add(cubeVertices[i + 2])
            colors.addAll(enemyColor.toList())
        }
        
        // Add eyes
        val eyeSize = halfSize * 0.3f
        val eyeOffset = halfSize * 0.7f
        
        // Left eye
        vertices.add(-eyeSize)
        vertices.add(eyeSize)
        vertices.add(eyeOffset)
        colors.addAll(enemyEyeColor.toList())
        
        // Right eye
        vertices.add(eyeSize)
        vertices.add(eyeSize)
        vertices.add(eyeOffset)
        colors.addAll(enemyEyeColor.toList())
        
        // Cube indices
        val cubeIndices = shortArrayOf(
            0, 1, 2, 0, 2, 3,    // front
            4, 5, 6, 4, 6, 7,    // back
            8, 9, 10, 8, 10, 11, // top
            12, 13, 14, 12, 14, 15, // bottom
            16, 17, 18, 16, 18, 19, // right
            20, 21, 22, 20, 22, 23  // left
        )
        
        indices.addAll(cubeIndices.toList())
        
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
        if (!alive) return
        
        animationTime += deltaTime
        bobOffset = sin(animationTime * 4f) * 0.05f
        y = 0.3f + bobOffset
        
        val distanceToPlayer = sqrt((x - playerX) * (x - playerX) + (z - playerZ) * (z - playerZ))
        
        // Update AI state
        when (aiState) {
            AIState.PATROL -> {
                if (distanceToPlayer < detectionRange) {
                    aiState = AIState.CHASE
                } else {
                    patrol(deltaTime)
                }
            }
            AIState.CHASE -> {
                if (distanceToPlayer > detectionRange * 1.5f) {
                    aiState = AIState.PATROL
                    generatePatrolTarget()
                } else if (distanceToPlayer < attackRange) {
                    aiState = AIState.ATTACK
                } else {
                    chase(playerX, playerZ, deltaTime)
                }
            }
            AIState.ATTACK -> {
                if (distanceToPlayer > attackRange) {
                    aiState = AIState.CHASE
                } else {
                    attack(deltaTime)
                }
            }
            AIState.STUNNED -> {
                // Stunned state - no movement
            }
        }
        
        // Apply movement
        x += velocityX * deltaTime
        z += velocityZ * deltaTime
        
        // Decay velocity
        velocityX *= 0.9f
        velocityZ *= 0.9f
    }
    
    private fun patrol(deltaTime: Float) {
        val dx = targetX - x
        val dz = targetZ - z
        val distance = sqrt(dx * dx + dz * dz)
        
        if (distance < 0.5f) {
            generatePatrolTarget()
        } else {
            val moveSpeed = speed * 0.5f
            velocityX = (dx / distance) * moveSpeed
            velocityZ = (dz / distance) * moveSpeed
        }
    }
    
    private fun chase(playerX: Float, playerZ: Float, deltaTime: Float) {
        val dx = playerX - x
        val dz = playerZ - z
        val distance = sqrt(dx * dx + dz * dz)
        
        if (distance > 0.1f) {
            velocityX = (dx / distance) * speed
            velocityZ = (dz / distance) * speed
        }
    }
    
    private fun attack(deltaTime: Float) {
        lastAttackTime += deltaTime
        if (lastAttackTime >= attackCooldown) {
            // Perform attack
            lastAttackTime = 0f
            // Attack logic would be handled by game engine
        }
    }
    
    private fun generatePatrolTarget() {
        val angle = Random.nextFloat() * 2 * PI
        targetX = originalX + cos(angle).toFloat() * patrolRadius
        targetZ = originalZ + sin(angle).toFloat() * patrolRadius
    }
    
    fun draw(mvpMatrix: FloatArray) {
        if (!alive || vertexBuffer == null || colorBuffer == null || indexBuffer == null) return
        
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
        
        // Draw the enemy
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    fun takeDamage(damage: Float) {
        health -= damage
        if (health <= 0) {
            alive = false
            health = 0f
        }
        aiState = AIState.STUNNED
        // Stun effect would wear off after a short time
    }
    
    fun checkCollision(otherX: Float, otherY: Float, otherZ: Float, otherRadius: Float): Boolean {
        if (!alive) return false
        
        val dx = x - otherX
        val dy = y - otherY
        val dz = z - otherZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        
        return distance < (size + otherRadius)
    }
    
    fun canAttack(): Boolean {
        return alive && aiState == AIState.ATTACK && lastAttackTime >= attackCooldown
    }
    
    fun getAttackDamage() = attackDamage
    fun isAlive() = alive
    fun getHealth() = health
    fun getMaxHealth() = maxHealth
    fun getSize() = size
}

