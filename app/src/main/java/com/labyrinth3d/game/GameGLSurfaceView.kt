package com.labyrinth3d.game

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class GameGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {
    
    private lateinit var renderer: GameRenderer
    private var gameEngine: GameEngine? = null
    
    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
        
        renderer = GameRenderer(context)
        setRenderer(renderer)
        
        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    
    fun setGameEngine(engine: GameEngine) {
        this.gameEngine = engine
        renderer.setGameEngine(engine)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Handle touch input for additional controls if needed
                gameEngine?.onTouch(event.x, event.y)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

