package com.example.beaconscanner.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.beaconscanner.model.Point
import com.example.beaconscanner.utils.CoordinateCaculator
import kotlin.math.sin

class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private val renderer: MyGLRenderer
//    private val myGL20Renderer:MyGL20Renderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(context)
//        myGL20Renderer = MyGL20Renderer(context)
        setRenderer(renderer)
//        setRenderer(myGL20Renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        return true
    }

    fun setAngle(value: Float) {
        renderer.angle = value
    }
    fun setTarget(angle:Float,speed:Float){
        renderer.target = CoordinateCaculator.calculatePointByDistanceAndAngle(renderer.target,speed,angle)
    }
    fun setEyeX(value: Float) {
        renderer.eyex = value
    }

    fun setEyeY(value: Float) {
        renderer.eyey = value
    }

    fun setEyeZ(value: Float) {
        renderer.eyez = value
    }
    fun setCenterX(value: Float) {
        renderer.centerX = value
    }
    fun setCenterY(value: Float) {
        renderer.centerY = value
    }
    fun setCenterZ(value: Float) {
        renderer.centerZ = value
    }

    fun setTranlate(value: Float) {
        renderer.tranlate = value
    }
    fun setTargetCoordinate(point: Point){
        renderer.target = Point(point.x,point.y)
    }
    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = -0.005F
    }
}