package com.example.beaconscanner.views.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.beaconscanner.controller.ToolController
import com.example.beaconscanner.model.Point
import com.example.beaconscanner.model.camera.CameraCenter
import com.example.beaconscanner.model.camera.CameraEye
import com.example.beaconscanner.model.camera.CameraUp
import com.example.beaconscanner.model.shapes.*
import com.example.beaconscanner.utils.ColorUntil
import com.example.beaconscanner.utils.CoordinateCaculator
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRender(context: Context) : GLSurfaceView.Renderer {
    @Volatile
    var mode = 0

    @Volatile
    var ratio: Float = 0f

    @Volatile
    var zoom: Float = 0.3f

    @Volatile
    var angle = 0f

    @Volatile
    var cameraEye = CameraEye(0f, 0f, 5f)

    @Volatile
    var cameraCenter = CameraCenter(0f, 0f, 0f)

    @Volatile
    var cameraUp = CameraUp(0f, 1f, 0f)

    @Volatile
    lateinit var beacon1: Square

    @Volatile
    lateinit var beacon2: Square

    @Volatile
    lateinit var beacon3: Square

    @Volatile
    lateinit var person: Square

    @Volatile
    lateinit var beacon1Circle: Circle

    @Volatile
    lateinit var beacon2Circle: Circle

    @Volatile
    lateinit var beacon3Circle: Circle

    @Volatile
    var distance1: Float = 0f

    @Volatile
    var distance2: Float = 0f

    @Volatile
    var distance3: Float = 0f

    @Volatile
    var tranlateCoordinate = Point(0f, 0f)
    var maxZoom  = 10f
    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val context: Context = context
    private var lines: ArrayList<Line> = arrayListOf()

    //create model
//    private lateinit var map: Demo
    lateinit var map: MapShape
    private val lineFrameSize = 50
    lateinit var drawer: Square
    var beaconStart = false
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        // initialize a model
        for (x in -lineFrameSize..lineFrameSize) {
            lines.add(Line(x.toFloat(), Line.HORIZONTAL, lineFrameSize.toFloat()))
        }
        for (y in -lineFrameSize..lineFrameSize) {
            lines.add(Line(y.toFloat(), Line.VERTICAL, lineFrameSize.toFloat()))
        }

        beacon1 = Square(-0.53657126f, -0.42181498f, 0.3f, ColorUntil.RED)
        beacon2 = Square(-0.40882167f, -8.921436f, 0.3f, ColorUntil.GREEN)
        beacon3 = Square(1.7988465f, -9.421336f, 0.3f, ColorUntil.BLUE)
        person = Square(0f, 0f, 0.2f, ColorUntil.PINK)
        beacon1Circle = Circle(beacon1.position.x, beacon1.position.y, 1f, ColorUntil.RED)
        beacon2Circle = Circle(beacon2.position.x, beacon2.position.y, 1f, ColorUntil.GREEN)
        beacon3Circle = Circle(beacon3.position.x, beacon3.position.y, 1f, ColorUntil.BLUE)
//        map = Demo(context,587f,801f,10f,"mapc302","png")
        drawer = Square(0f, 0f, 0.2f, ColorUntil.RED)
        map = MapShape()
        unused.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, zoom, maxZoom)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            viewMatrix,
            0,
            cameraEye.x,
            cameraEye.y,
            cameraEye.z,
            cameraCenter.x,
            cameraCenter.y,
            cameraCenter.z,
            cameraUp.x,
            cameraUp.y,
            cameraUp.z
        )

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(vPMatrix, 0, tranlateCoordinate.x, tranlateCoordinate.y, 0f)
        //draw model
        for (item in lines) {
            if (lines.indexOf(item) == 50 || lines.indexOf(item) == 151) {
                item.setColor(ColorUntil.VIOLET)
            }
            item.draw(vPMatrix)
        }
        map.draw(vPMatrix)
        if (mode == ToolController.DRAW_MAP) {
            drawer.draw(vPMatrix)
        } else {
            if (beaconStart) {
                beacon1Circle.setSize(distance1)
                beacon1Circle.setMiddlePoint(beacon1.position)
                beacon1Circle.draw(vPMatrix)

                beacon2Circle.setSize(distance2)
                beacon2Circle.setMiddlePoint(beacon2.position)
                beacon2Circle.draw(vPMatrix)

                beacon3Circle.setSize(distance3)
                beacon3Circle.setMiddlePoint(beacon3.position)
                beacon3Circle.draw(vPMatrix)
                try {
                    val newpoint = CoordinateCaculator.getTrilateration(
                        beacon1Circle.position,
                        distance1,
                        beacon2Circle.position,
                        distance2,
                        beacon3Circle.position,
                        distance3
                    )
                    person.setPositionByCoordinate(newpoint)
                    person.draw(vPMatrix)
                } catch (e: java.lang.Exception) {

                }
            }
            beacon1.draw(vPMatrix)
            beacon2.draw(vPMatrix)
            beacon3.draw(vPMatrix)
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        ratio = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, zoom, maxZoom)
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    }

}