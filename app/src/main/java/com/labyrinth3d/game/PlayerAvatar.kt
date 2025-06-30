package com.labyrinth3d.game

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class PlayerAvatar : GameObject() {
    // Make position properties public
    public override var x: Float
        get() = super.x
        set(value) { super.x = value }
    
    public override var y: Float
        get() = super.y
        set(value) { super.y = value }
    
    public override var z: Float
        get() = super.z
        set(value) { super.z = value }
    private var velocityX = 0f
    private var velocityZ = 0f
    private var animationTime = 0f
    private var walkCycle = 0f
    private var isWalking = false
    private var health = 100f
    private var maxHealth = 100f
    private var experience = 0
    private var level = 1
    private var jumpHeight = 0f
    private var isJumping = false
    private var jumpVelocity = 0f
    private var vertexCount = 0
    
    // Avatar appearance
    private var avatarColor = floatArrayOf(0.2f, 0.6f, 1.0f, 1.0f) // Blue avatar
    private var headSize = 0.3f
    private var bodyHeight = 0.8f
    private var armLength = 0.4f
    private var legLength = 0.6f
    
    // Movement properties
    private val maxSpeed = 3.0f
    private val acceleration = 8.0f
    private val friction = 6.0f
    private val jumpPower = 5.0f
    private val gravity = -12.0f
    
    // Animation states
    enum class AnimationState {
        IDLE, WALKING, RUNNING, JUMPING, ATTACKING, CELEBRATING
    }
    private var currentAnimation = AnimationState.IDLE
    
    override val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;
        
        void main() {
            // Add subtle breathing animation when idle
            vec4 pos = vPosition;
            if (abs(pos.y - 0.4) < 0.1) { // Body area
                pos.y += sin(uTime * 2.0) * 0.02;
            }
            
            gl_Position = uMVPMatrix * pos;
            fColor = vColor;
        }
    """.trimIndent()
    
    override val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;
        uniform float uTime;
        
        void main() {
            // Add subtle glow effect
            vec3 color = fColor.rgb;
            float glow = 1.0 + sin(uTime * 3.0) * 0.1;
            gl_FragColor = vec4(color * glow, fColor.a);
        }
    """.trimIndent()
    
    init {
        initializeAvatar()
    }
    
    override fun initializeBuffers() {
        // This is handled in createAvatarGeometry()
    }
    
    override fun drawGeometry() {
        // Draw the avatar geometry
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }
    
    private fun initializeAvatar() {
        createAvatarGeometry()
        initializeShaders()
        
        // Set initial position
        setPosition(0f, 0f, 0f)
    }
    
    private fun createAvatarGeometry() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        
        // Create 3D humanoid avatar
        createHead(vertices, colors)
        createBody(vertices, colors)
        createArms(vertices, colors)
        createLegs(vertices, colors)
        
        // Convert to FloatBuffer
        val vertexArray = vertices.toFloatArray()
        val colorArray = colors.toFloatArray()
        
        // Set vertex count (3 coordinates per vertex)
        vertexCount = vertexArray.size / 3
        
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
    }
    
    private fun createHead(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val headY = bodyHeight + headSize
        createSphere(vertices, colors, 0f, headY, 0f, headSize, avatarColor)
        
        // Eyes
        val eyeColor = floatArrayOf(1f, 1f, 1f, 1f)
        createSphere(vertices, colors, -0.1f, headY + 0.05f, headSize * 0.8f, 0.05f, eyeColor)
        createSphere(vertices, colors, 0.1f, headY + 0.05f, headSize * 0.8f, 0.05f, eyeColor)
        
        // Pupils
        val pupilColor = floatArrayOf(0f, 0f, 0f, 1f)
        createSphere(vertices, colors, -0.1f, headY + 0.05f, headSize * 0.85f, 0.02f, pupilColor)
        createSphere(vertices, colors, 0.1f, headY + 0.05f, headSize * 0.85f, 0.02f, pupilColor)
    }
    
    private fun createBody(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val bodyWidth = 0.4f
        val bodyDepth = 0.2f
        createBox(vertices, colors, 0f, bodyHeight/2, 0f, bodyWidth, bodyHeight, bodyDepth, avatarColor)
    }
    
    private fun createArms(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val armWidth = 0.1f
        val armY = bodyHeight * 0.7f
        
        // Left arm
        createBox(vertices, colors, -0.3f, armY, 0f, armWidth, armLength, armWidth, avatarColor)
        // Right arm  
        createBox(vertices, colors, 0.3f, armY, 0f, armWidth, armLength, armWidth, avatarColor)
        
        // Hands
        val handColor = floatArrayOf(0.8f, 0.6f, 0.4f, 1f)
        createSphere(vertices, colors, -0.3f, armY - armLength/2, 0f, 0.08f, handColor)
        createSphere(vertices, colors, 0.3f, armY - armLength/2, 0f, 0.08f, handColor)
    }
    
    private fun createLegs(vertices: MutableList<Float>, colors: MutableList<Float>) {
        val legWidth = 0.12f
        val legY = -legLength/2
        
        // Left leg
        createBox(vertices, colors, -0.15f, legY, 0f, legWidth, legLength, legWidth, avatarColor)
        // Right leg
        createBox(vertices, colors, 0.15f, legY, 0f, legWidth, legLength, legWidth, avatarColor)
        
        // Feet
        val footColor = floatArrayOf(0.1f, 0.1f, 0.1f, 1f)
        createBox(vertices, colors, -0.15f, -legLength, 0.1f, 0.2f, 0.1f, 0.3f, footColor)
        createBox(vertices, colors, 0.15f, -legLength, 0.1f, 0.2f, 0.1f, 0.3f, footColor)
    }
    
    private fun createSphere(vertices: MutableList<Float>, colors: MutableList<Float>, 
                           centerX: Float, centerY: Float, centerZ: Float, 
                           radius: Float, color: FloatArray) {
        val segments = 8
        val rings = 6
        
        for (ring in 0 until rings) {
            val phi1 = PI * ring / rings
            val phi2 = PI * (ring + 1) / rings
            
            for (segment in 0 until segments) {
                val theta1 = 2 * PI * segment / segments
                val theta2 = 2 * PI * (segment + 1) / segments
                
                // Create quad as two triangles
                val v1 = sphereVertex(centerX, centerY, centerZ, radius, phi1, theta1)
                val v2 = sphereVertex(centerX, centerY, centerZ, radius, phi1, theta2)
                val v3 = sphereVertex(centerX, centerY, centerZ, radius, phi2, theta1)
                val v4 = sphereVertex(centerX, centerY, centerZ, radius, phi2, theta2)
                
                // Triangle 1
                vertices.addAll(v1)
                vertices.addAll(v2)
                vertices.addAll(v3)
                
                // Triangle 2
                vertices.addAll(v2)
                vertices.addAll(v4)
                vertices.addAll(v3)
                
                // Colors for 6 vertices
                repeat(6) { colors.addAll(color.toList()) }
            }
        }
    }
    
    private fun sphereVertex(centerX: Float, centerY: Float, centerZ: Float, 
                           radius: Float, phi: Double, theta: Double): List<Float> {
        val x = centerX + radius * sin(phi) * cos(theta)
        val y = centerY + radius * cos(phi)
        val z = centerZ + radius * sin(phi) * sin(theta)
        return listOf(x.toFloat(), y.toFloat(), z.toFloat())
    }
    
    private fun createBox(vertices: MutableList<Float>, colors: MutableList<Float>,
                         centerX: Float, centerY: Float, centerZ: Float,
                         width: Float, height: Float, depth: Float, color: FloatArray) {
        val halfW = width / 2
        val halfH = height / 2
        val halfD = depth / 2
        
        val boxVertices = floatArrayOf(
            // Front face
            centerX - halfW, centerY - halfH, centerZ + halfD,
            centerX + halfW, centerY - halfH, centerZ + halfD,
            centerX + halfW, centerY + halfH, centerZ + halfD,
            centerX - halfW, centerY - halfH, centerZ + halfD,
            centerX + halfW, centerY + halfH, centerZ + halfD,
            centerX - halfW, centerY + halfH, centerZ + halfD,
            
            // Back face
            centerX + halfW, centerY - halfH, centerZ - halfD,
            centerX - halfW, centerY - halfH, centerZ - halfD,
            centerX + halfW, centerY + halfH, centerZ - halfD,
            centerX + halfW, centerY + halfH, centerZ - halfD,
            centerX - halfW, centerY - halfH, centerZ - halfD,
            centerX - halfW, centerY + halfH, centerZ - halfD,
            
            // Top face
            centerX - halfW, centerY + halfH, centerZ - halfD,
            centerX + halfW, centerY + halfH, centerZ - halfD,
            centerX + halfW, centerY + halfH, centerZ + halfD,
            centerX - halfW, centerY + halfH, centerZ - halfD,
            centerX + halfW, centerY + halfH, centerZ + halfD,
            centerX - halfW, centerY + halfH, centerZ + halfD
        )
        
        vertices.addAll(boxVertices.toList())
        repeat(18) { colors.addAll(color.toList()) }
    }
    
    fun update(deltaTime: Float, inputX: Float, inputZ: Float, jumpPressed: Boolean) {
        animationTime += deltaTime
        
        // Handle input and movement
        handleMovement(deltaTime, inputX, inputZ)
        handleJump(deltaTime, jumpPressed)
        updateAnimation(deltaTime)
        updatePhysics(deltaTime)
        
        // Update experience and level
        if (isWalking) {
            experience += (deltaTime * 10).toInt()
            if (experience >= level * 100) {
                levelUp()
            }
        }
    }
    
    private fun handleMovement(deltaTime: Float, inputX: Float, inputZ: Float) {
        val inputMagnitude = sqrt(inputX * inputX + inputZ * inputZ)
        
        if (inputMagnitude > 0.1f) {
            // Normalize input
            val normalizedX = inputX / inputMagnitude
            val normalizedZ = inputZ / inputMagnitude
            
            // Apply acceleration
            velocityX += normalizedX * acceleration * deltaTime
            velocityZ += normalizedZ * acceleration * deltaTime
            
            // Limit speed
            val currentSpeed = sqrt(velocityX * velocityX + velocityZ * velocityZ)
            if (currentSpeed > maxSpeed) {
                velocityX = (velocityX / currentSpeed) * maxSpeed
                velocityZ = (velocityZ / currentSpeed) * maxSpeed
            }
            
            isWalking = true
            currentAnimation = if (currentSpeed > maxSpeed * 0.7f) AnimationState.RUNNING else AnimationState.WALKING
            
            // Update rotation to face movement direction
            rotation = atan2(velocityX, velocityZ) * 180f / PI.toFloat()
        } else {
            // Apply friction
            velocityX *= (1f - friction * deltaTime).coerceAtLeast(0f)
            velocityZ *= (1f - friction * deltaTime).coerceAtLeast(0f)
            
            if (abs(velocityX) < 0.1f && abs(velocityZ) < 0.1f) {
                velocityX = 0f
                velocityZ = 0f
                isWalking = false
                currentAnimation = AnimationState.IDLE
            }
        }
        
        // Update position
        x += velocityX * deltaTime
        z += velocityZ * deltaTime
    }
    
    private fun handleJump(deltaTime: Float, jumpPressed: Boolean) {
        if (jumpPressed && !isJumping && jumpHeight <= 0.1f) {
            isJumping = true
            jumpVelocity = jumpPower
            currentAnimation = AnimationState.JUMPING
        }
        
        if (isJumping) {
            jumpHeight += jumpVelocity * deltaTime
            jumpVelocity += gravity * deltaTime
            
            if (jumpHeight <= 0f) {
                jumpHeight = 0f
                jumpVelocity = 0f
                isJumping = false
                if (!isWalking) currentAnimation = AnimationState.IDLE
            }
        }
        
        y = jumpHeight
    }
    
    private fun updateAnimation(deltaTime: Float) {
        when (currentAnimation) {
            AnimationState.WALKING -> {
                walkCycle += deltaTime * 4f
            }
            AnimationState.RUNNING -> {
                walkCycle += deltaTime * 6f
            }
            AnimationState.IDLE -> {
                walkCycle = 0f
            }
            else -> {}
        }
    }
    
    private fun updatePhysics(deltaTime: Float) {
        // Health regeneration
        if (health < maxHealth) {
            health = (health + deltaTime * 5f).coerceAtMost(maxHealth)
        }
    }
    
    private fun levelUp() {
        level++
        maxHealth += 20f
        health = maxHealth
        currentAnimation = AnimationState.CELEBRATING
        // Reset experience for next level
        experience = 0
    }
    
    fun takeDamage(damage: Float) {
        health = (health - damage).coerceAtLeast(0f)
    }
    
    fun heal(amount: Float) {
        health = (health + amount).coerceAtMost(maxHealth)
    }
    
    override fun draw(mvpMatrix: FloatArray) {
        if (!active) return
        
        GLES20.glUseProgram(program)
        
        // Calculate model matrix with animation
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.rotateM(modelMatrix, 0, rotation, 0f, 1f, 0f)
        
        // Apply walking animation
        if (currentAnimation == AnimationState.WALKING || currentAnimation == AnimationState.RUNNING) {
            val bobAmount = sin(walkCycle) * 0.05f
            Matrix.translateM(modelMatrix, 0, 0f, bobAmount, 0f)
        }
        
        // Calculate MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
        
        // Get shader handles
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        
        // Set vertex data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        
        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, animationTime)
        
        // Draw the avatar
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffer!!.capacity() / 3)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
    
    // Getters for game state
    fun getHealth() = health
    fun getMaxHealth() = maxHealth
    fun getLevel() = level
    fun getExperience() = experience
    fun getExperienceForNextLevel() = level * 100
    fun isAlive() = health > 0f
    fun getSpeed() = sqrt(velocityX * velocityX + velocityZ * velocityZ)
    
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
}
