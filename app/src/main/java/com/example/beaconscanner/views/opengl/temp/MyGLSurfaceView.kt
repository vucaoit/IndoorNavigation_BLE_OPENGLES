package com.example.beaconscanner.views.opengl.temp//package com.example.beaconscanner.opengl
//
//import android.content.Context
//import android.opengl.GLSurfaceView
//import android.util.AttributeSet
//import android.util.Log
//import android.view.MotionEvent
//import com.example.beaconscanner.model.Point
//import com.example.beaconscanner.utils.CoordinateCaculator
//import kotlin.math.sqrt
//
//
//class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {
//    var lastZoom = 3f
//    var startZoom = 0f
//    private val mPreviousX = 0f
//    private val mPreviousY = 0f
//    private val renderer: MyGLRenderer
////    private val myGL20Renderer:MyGL20Renderer
//    private val openGLRender: OpenGLRender
//
//    init {
//        setEGLContextClientVersion(2)
//        renderer = MyGLRenderer(context)
////        myGL20Renderer = MyGL20Renderer(context)
//        openGLRender = OpenGLRender(context)
//        setRenderer(openGLRender)
////        setRenderer(myGL20Renderer)
//        renderMode = RENDERMODE_WHEN_DIRTY
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
////        val x: Float = event.getX()
////        val y: Float = event.getY()
////        if(event.pointerCount == 1){
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> {
////                    Log.e("MOTION", "Start")
//////                    startPoint = Point(event.x,event.y)
////                }
////
////                MotionEvent.ACTION_MOVE -> {
////
////                }
////                MotionEvent.ACTION_UP -> {
////                    Log.e("MOTION", "Ended")
////                }
////            }
////        }
////        if (event.pointerCount == 2) {
////            when (event.action) {
////                MotionEvent.ACTION_POINTER_2_DOWN -> {
////                    Log.e("MOTION", "Start")
////                    startZoom = getDistance(event) / 1000f
////                }
////                MotionEvent.ACTION_POINTER_UP -> {
////                    Log.e("MOTION", "Ended")
////                    lastZoom = renderer.zoom
////                    startZoom = 0f
////                }
////                MotionEvent.ACTION_MOVE -> {
////                    val distance = getDistance(event) / 1000f
////                    val tiLe = distance / startZoom
////                    Log.e("MOTION", "startZoom : ${startZoom},distance : $distance, Ratio : $tiLe")
////
//////                    if (tiLe < 0) {
////                        renderer.zoom = lastZoom*tiLe
//////                    } else {
//////                        renderer.zoom = lastZoom/tiLe
//////                    }
////
////                }
////            }
////
////            requestRender()
////        }
//        return true
//    }
//
//    fun getDistance(event: MotionEvent): Float {
//        val dx = (event.getX(0) - event.getX(1)).toInt()
//        val dy = (event.getY(0) - event.getY(1)).toInt()
//        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
//    }
//
//    fun setAngle(value: Float) {
//        renderer.angle = value
//    }
//
//    fun setTarget(angle: Float, speed: Float) {
//        renderer.target =
//            CoordinateCaculator.calculatePointByDistanceAndAngle(renderer.target, speed, angle)
//    }
//
//    fun setEyeX(value: Float) {
//        renderer.eyex = value
//    }
//
//    fun setEyeY(value: Float) {
//        renderer.eyey = value
//    }
//
//    fun setEyeZ(value: Float) {
//        renderer.eyez = value
//    }
//
//    fun setCenterX(value: Float) {
//        renderer.centerX = value
//    }
//
//    fun setCenterY(value: Float) {
//        renderer.centerY = value
//    }
//
//    fun setCenterZ(value: Float) {
//        renderer.centerZ = value
//    }
//
//    fun setTranlate(value: Float) {
//        renderer.tranlate = value
//    }
//
//    fun setTargetCoordinate(point: Point) {
//        renderer.target = Point(point.x, point.y)
//    }
//
//    companion object {
//        private const val TOUCH_SCALE_FACTOR: Float = -0.005F
//    }
//}