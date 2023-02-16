package com.example.beaconscanner.views.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.beaconscanner.model.Point
import com.example.beaconscanner.model.camera.CameraCenter
import com.example.beaconscanner.model.camera.CameraEye
import com.example.beaconscanner.model.camera.CameraUp
import com.example.beaconscanner.model.shapes.Circle
import com.example.beaconscanner.model.shapes.Demo
import com.example.beaconscanner.model.shapes.Line
import com.example.beaconscanner.model.shapes.Square
import com.example.beaconscanner.utils.ColorUntil
import com.example.beaconscanner.utils.CoordinateCaculator
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRender(context: Context) : GLSurfaceView.Renderer {
    @Volatile
    var ratio:Float = 0f
    @Volatile
    var zoom:Float = 3f
    @Volatile
    var angle = 0f
    @Volatile
    var cameraEye = CameraEye(0f,0f,5f)
    @Volatile
    var cameraCenter = CameraCenter(0f,0f,0f)
    @Volatile
    var cameraUp = CameraUp(0f,1f,0f)
    @Volatile
    lateinit var beacon1:Square
    @Volatile
    lateinit var beacon2 :Square
    @Volatile
    lateinit var beacon3 :Square
    @Volatile
    lateinit var person :Square
    @Volatile
    lateinit var beacon1Circle: Circle
    @Volatile
    lateinit var beacon2Circle: Circle
    @Volatile
    lateinit var beacon3Circle: Circle
    @Volatile
    var distance1:Float = 0f
    @Volatile
    var distance2:Float = 0f
    @Volatile
    var distance3:Float = 0f

    @Volatile
    var tranlateCoordinate = Point(0f,0f)
    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val context: Context = context
    //create model
    private lateinit var map: Demo

    private val lineFrameSize = 50
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        // initialize a model
//        for(x in -20..20){
//            lines.add(Line(x.toFloat(),Line.HORIZONTAL, 10f/2))
//        }
//        for(y in -10..10){
//            lines.add(Line(y.toFloat(),Line.VERTICAL, 20f/2))
//        }
        beacon1 = Square(0f,0f,0.02f,ColorUntil.RED)
        beacon2 = Square(0f,0f,0.02f,ColorUntil.GREEN)
        beacon3 = Square(0f,0f,0.02f,ColorUntil.BLUE)
        person = Square(0f,0f,0.02f,ColorUntil.ORANGE)
        beacon1Circle = Circle(beacon1.position.x,beacon1.position.y,1f,ColorUntil.RED)
        beacon2Circle = Circle(beacon2.position.x,beacon2.position.y,1f,ColorUntil.GREEN)
        beacon3Circle = Circle(beacon3.position.x,beacon3.position.y,1f,ColorUntil.BLUE)
        map = Demo(context,587f,801f,1f,"mapc302","png")
        unused.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, zoom, 200f)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, cameraEye.x,cameraEye.y,cameraEye.z,cameraCenter.x,cameraCenter.y,cameraCenter.z,cameraUp.x,cameraUp.y,cameraUp.z)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(vPMatrix,0,tranlateCoordinate.x,tranlateCoordinate.y,0f)
        //draw model
        map.draw(vPMatrix)
        beacon1.draw(vPMatrix)
        beacon2.draw(vPMatrix)
        beacon3.draw(vPMatrix)

        beacon1Circle.setSize(distance1)
        beacon1Circle.setMiddlePoint(beacon1.position)
        beacon1Circle.draw(vPMatrix)

        beacon2Circle.setSize(distance2)
        beacon2Circle.setMiddlePoint(beacon2.position)
        beacon2Circle.draw(vPMatrix)

        beacon3Circle.setSize(distance3)
        beacon3Circle.setMiddlePoint(beacon3.position)
        beacon3Circle.draw(vPMatrix)

        try{
            val newpoint = CoordinateCaculator.getTrilateration(beacon1Circle.position,distance1,beacon2Circle.position,distance2,beacon3Circle.position,distance3)
            person.setPositionByCoordinate(newpoint)
            person.draw(vPMatrix)
        }catch (e:java.lang.Exception){

        }
//        Log.e("DISTANCE 123","${distance1},${distance2},${distance3}")
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        ratio = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, zoom, 200f)
    }
    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    }

}