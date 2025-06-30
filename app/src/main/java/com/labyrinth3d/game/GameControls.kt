package com.labyrinth3d.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GameControls @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface ControlListener {
        fun onMovement(x: Float, z: Float)
        fun onJump()
        fun onAction()
        fun onPauseGame()
        fun onSpecialAbility()
    }

    private var controlListener: ControlListener? = null
    
    // Virtual joystick properties
    private var joystickCenterX = 0f
    private var joystickCenterY = 0f
    private var joystickRadius = 100f
    private var knobRadius = 40f
    private var knobX = 0f
    private var knobY = 0f
    private var joystickPressed = false
    private var joystickPointerId = -1
    
    // Action buttons
    private val buttons = mutableMapOf<String, GameButton>()
    
    // Paint objects
    private val joystickBasePaint = Paint().apply {
        color = Color.argb(100, 255, 255, 255)
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val joystickKnobPaint = Paint().apply {
        color = Color.argb(150, 0, 150, 255)
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val buttonPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 40f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }
    
    // Gesture detection
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var swipeThreshold = 100f
    private var swipeVelocityThreshold = 100f
    
    data class GameButton(
        val x: Float,
        val y: Float,
        val radius: Float,
        val label: String,
        val color: Int,
        val action: String,
        var pressed: Boolean = false,
        var pointerId: Int = -1
    )
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupControls(w, h)
    }
    
    private fun setupControls(width: Int, height: Int) {
        // Position virtual joystick on the left side
        joystickCenterX = width * 0.15f
        joystickCenterY = height * 0.75f
        joystickRadius = minOf(width, height) * 0.08f
        knobRadius = joystickRadius * 0.4f
        
        // Reset knob position
        knobX = joystickCenterX
        knobY = joystickCenterY
        
        // Setup action buttons on the right side
        buttons.clear()
        
        val buttonRadius = minOf(width, height) * 0.06f
        val rightSide = width * 0.85f
        val bottomArea = height * 0.8f
        
        // Jump button
        buttons["jump"] = GameButton(
            rightSide, bottomArea - buttonRadius * 3,
            buttonRadius, "↑", Color.argb(200, 0, 255, 0), "jump"
        )
        
        // Action button
        buttons["action"] = GameButton(
            rightSide - buttonRadius * 2.5f, bottomArea,
            buttonRadius, "⚡", Color.argb(200, 255, 165, 0), "action"
        )
        
        // Special ability button
        buttons["special"] = GameButton(
            rightSide + buttonRadius * 0.5f, bottomArea,
            buttonRadius, "✦", Color.argb(200, 255, 0, 255), "special"
        )
        
        // Pause button (top right)
        buttons["pause"] = GameButton(
            width * 0.9f, height * 0.1f,
            buttonRadius * 0.8f, "⏸", Color.argb(200, 128, 128, 128), "pause"
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw virtual joystick
        drawJoystick(canvas)
        
        // Draw action buttons
        drawButtons(canvas)
        
        // Draw HUD elements
        drawHUD(canvas)
    }
    
    private fun drawJoystick(canvas: Canvas) {
        // Draw joystick base
        canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, joystickBasePaint)
        
        // Draw joystick border
        val borderPaint = Paint(joystickBasePaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.argb(150, 255, 255, 255)
        }
        canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, borderPaint)
        
        // Draw knob
        canvas.drawCircle(knobX, knobY, knobRadius, joystickKnobPaint)
        
        // Draw knob highlight
        val highlightPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawCircle(knobX - knobRadius * 0.3f, knobY - knobRadius * 0.3f, knobRadius * 0.3f, highlightPaint)
    }
    
    private fun drawButtons(canvas: Canvas) {
        for (button in buttons.values) {
            // Button background
            buttonPaint.color = if (button.pressed) {
                // Pressed state - brighter
                val color = button.color
                Color.argb(
                    Color.alpha(color),
                    minOf(255, (Color.red(color) * 1.3f).toInt()),
                    minOf(255, (Color.green(color) * 1.3f).toInt()),
                    minOf(255, (Color.blue(color) * 1.3f).toInt())
                )
            } else {
                button.color
            }
            
            canvas.drawCircle(button.x, button.y, button.radius, buttonPaint)
            
            // Button border
            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawCircle(button.x, button.y, button.radius, borderPaint)
            
            // Button label
            val textY = button.y + buttonTextPaint.textSize / 3
            canvas.drawText(button.label, button.x, textY, buttonTextPaint)
        }
    }
    
    private fun drawHUD(canvas: Canvas) {
        // Draw control hints
        val hintPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 255)
            textSize = 24f
            isAntiAlias = true
        }
        
        // Joystick hint
        canvas.drawText("Move", joystickCenterX, joystickCenterY + joystickRadius + 40f, hintPaint)
        
        // Button hints
        buttons["jump"]?.let { button ->
            canvas.drawText("Jump", button.x, button.y + button.radius + 30f, hintPaint)
        }
        
        buttons["action"]?.let { button ->
            canvas.drawText("Action", button.x, button.y + button.radius + 30f, hintPaint)
        }
        
        buttons["special"]?.let { button ->
            canvas.drawText("Special", button.x, button.y + button.radius + 30f, hintPaint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                handleTouchDown(event.getX(pointerIndex), event.getY(pointerIndex), pointerId)
            }
            
            MotionEvent.ACTION_MOVE -> {
                handleTouchMove(event)
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                handleTouchUp(pointerId)
            }
            
            MotionEvent.ACTION_CANCEL -> {
                handleTouchCancel()
            }
        }
        
        return true
    }
    
    private fun handleTouchDown(x: Float, y: Float, pointerId: Int) {
        // Check if touch is on joystick
        val joystickDistance = sqrt((x - joystickCenterX).pow(2) + (y - joystickCenterY).pow(2))
        if (joystickDistance <= joystickRadius && joystickPointerId == -1) {
            joystickPressed = true
            joystickPointerId = pointerId
            updateJoystickKnob(x, y)
            return
        }
        
        // Check if touch is on any button
        for (button in buttons.values) {
            val buttonDistance = sqrt((x - button.x).pow(2) + (y - button.y).pow(2))
            if (buttonDistance <= button.radius && button.pointerId == -1) {
                button.pressed = true
                button.pointerId = pointerId
                handleButtonPress(button.action)
                invalidate()
                return
            }
        }
        
        // Store for gesture detection
        lastTouchX = x
        lastTouchY = y
    }
    
    private fun handleTouchMove(event: MotionEvent) {
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            
            // Update joystick if this pointer controls it
            if (pointerId == joystickPointerId && joystickPressed) {
                updateJoystickKnob(x, y)
            }
        }
    }
    
    private fun handleTouchUp(pointerId: Int) {
        // Release joystick if this pointer controlled it
        if (pointerId == joystickPointerId) {
            joystickPressed = false
            joystickPointerId = -1
            // Return knob to center
            knobX = joystickCenterX
            knobY = joystickCenterY
            controlListener?.onMovement(0f, 0f)
            invalidate()
        }
        
        // Release any button controlled by this pointer
        for (button in buttons.values) {
            if (button.pointerId == pointerId) {
                button.pressed = false
                button.pointerId = -1
                invalidate()
            }
        }
    }
    
    private fun handleTouchCancel() {
        // Reset all controls
        joystickPressed = false
        joystickPointerId = -1
        knobX = joystickCenterX
        knobY = joystickCenterY
        
        for (button in buttons.values) {
            button.pressed = false
            button.pointerId = -1
        }
        
        controlListener?.onMovement(0f, 0f)
        invalidate()
    }
    
    private fun updateJoystickKnob(touchX: Float, touchY: Float) {
        val deltaX = touchX - joystickCenterX
        val deltaY = touchY - joystickCenterY
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
        
        if (distance <= joystickRadius) {
            knobX = touchX
            knobY = touchY
        } else {
            // Constrain knob to joystick boundary
            val angle = atan2(deltaY, deltaX)
            knobX = joystickCenterX + cos(angle) * joystickRadius
            knobY = joystickCenterY + sin(angle) * joystickRadius
        }
        
        // Calculate normalized movement values
        val normalizedX = (knobX - joystickCenterX) / joystickRadius
        val normalizedY = (knobY - joystickCenterY) / joystickRadius
        
        // Send movement to listener (invert Y for game coordinates)
        controlListener?.onMovement(normalizedX, -normalizedY)
        invalidate()
    }
    
    private fun handleButtonPress(action: String) {
        when (action) {
            "jump" -> controlListener?.onJump()
            "action" -> controlListener?.onAction()
            "special" -> controlListener?.onSpecialAbility()
            "pause" -> controlListener?.onPauseGame()
        }
    }
    
    fun setControlListener(listener: ControlListener) {
        this.controlListener = listener
    }
    
    // Public methods for external control
    fun simulateMovement(x: Float, z: Float) {
        controlListener?.onMovement(x, z)
    }
    
    fun simulateJump() {
        controlListener?.onJump()
    }
    
    fun setJoystickEnabled(enabled: Boolean) {
        if (!enabled) {
            joystickPressed = false
            knobX = joystickCenterX
            knobY = joystickCenterY
            controlListener?.onMovement(0f, 0f)
        }
        invalidate()
    }
    
    fun setButtonEnabled(buttonName: String, enabled: Boolean) {
        buttons[buttonName]?.let { button ->
            if (!enabled) {
                button.pressed = false
                button.pointerId = -1
            }
        }
        invalidate()
    }
}
