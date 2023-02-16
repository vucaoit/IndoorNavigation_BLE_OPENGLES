package com.example.beaconscanner.views.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.example.beaconscanner.model.Point
import kotlin.math.sqrt

class OpenGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {
    private val openGLRender: OpenGLRender
    private var prevousZoom = 3f
    private var distanceAtStart = 0f
    private var prevousPoint = Point(0f, 0f)

    init {
        setEGLContextClientVersion(2)
        openGLRender = OpenGLRender(context)
        setRenderer(openGLRender)
//        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //action move
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    prevousPoint = Point(0f, -0f)
                    Log.d("Motion", "start")
                }
                MotionEvent.ACTION_MOVE -> {
                    var currentPoint = Point(event.x, event.y)
                    if (event.x < width / 2) {
                        //x mang gia tri am
                        currentPoint.x = -(width / 2 - event.x) * TOUCH_SCALE_FACTOR
                    } else {
                        //x mang gia tri duong
                        currentPoint.x = (event.x - width / 2) * TOUCH_SCALE_FACTOR
                    }
                    if (event.y < height / 2) {
                        //y mang gia tri duong
                        currentPoint.y = (height / 2 - event.y) * TOUCH_SCALE_FACTOR
                    } else {
                        //y mang gia tri am
                        currentPoint.y = -(event.y - height / 2) * TOUCH_SCALE_FACTOR
                    }
                    openGLRender.tranlateCoordinate = Point(
                        prevousPoint.x + currentPoint.x,
                        prevousPoint.y + currentPoint.y
                    )
//                    openGLRender.cameraEye.x = prevousPoint.x + currentPoint.x
//                    openGLRender.cameraEye.y = prevousPoint.y + currentPoint.y
                    Log.d("Motion", "Move")
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("Motion", "end")
                    prevousPoint = openGLRender.tranlateCoordinate
                }
            }
        }

        //action zoom
        if (event.pointerCount == 2) {
            when (event.action) {
                MotionEvent.ACTION_POINTER_2_DOWN -> {
//                    Log.e("MOTION", "Start")
                    distanceAtStart = getDistance(event) / 1000f

                }
                MotionEvent.ACTION_POINTER_UP -> {
//                    Log.e("MOTION", "Ended")
                    distanceAtStart = 0f
                    prevousZoom = openGLRender.zoom
                }
                MotionEvent.ACTION_MOVE -> {
//                    Log.e("MOTION", "Move")
                    val distance = getDistance(event) / 1000f
                    val tile = distance / distanceAtStart
                    if (prevousZoom * tile < 27) {
                        openGLRender.zoom = prevousZoom * tile
                    }
                    println(openGLRender.zoom)
                }
            }
            requestRender()
        }
        //action 3DView
        if (event.pointerCount == 3) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Motion", "starttttt")
                }
                MotionEvent.ACTION_MOVE -> {
                    openGLRender.cameraEye.y = -event.y / 30
                    if (event.x < width / 2) {
                        //x mang gia tri am
                        openGLRender.cameraEye.x = -(width / 2 - event.x) / 10
                    } else {
                        //x mang gia tri duong
                        openGLRender.cameraEye.x = (event.x - width / 2) / 10
                    }
                    Log.d("Motion", "movinggggg")
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("Motion", "enddddddd")
                }
            }
        }
        return true
    }

    fun setAngle(value: Float) {
        openGLRender.angle = value
    }

    fun beacon1SetPosition(point: Point) {
        openGLRender.beacon1.setPositionByCoordinate(point)
    }

    fun beacon1GetPosition(): Point {
        return openGLRender.beacon1.position
    }

    fun beacon2SetPosition(point: Point) {
        openGLRender.beacon2.setPositionByCoordinate(point)
    }

    fun beacon2GetPosition(): Point {
        return openGLRender.beacon2.position
    }

    fun beacon3SetPosition(point: Point) {
        openGLRender.beacon3.setPositionByCoordinate(point)
    }

    fun beacon3GetPosition(): Point {
        return openGLRender.beacon3.position
    }

    fun setDistance1(value: Float) {
        openGLRender.distance1 = value
    }

    fun setDistance2(value: Float) {
        openGLRender.distance2 = value
    }

    fun setDistance3(value: Float) {
        openGLRender.distance3 = value
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = (event.getX(0) - event.getX(1)).toInt()
        val dy = (event.getY(0) - event.getY(1)).toInt()
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 0.003F
    }
}