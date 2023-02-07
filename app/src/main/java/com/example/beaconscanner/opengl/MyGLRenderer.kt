package com.example.beaconscanner.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.beaconscanner.model.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MyGLRenderer(context: Context) : GLSurfaceView.Renderer {
    @Volatile
    var angle: Float = 0f

    @Volatile
    var x: Float = 0f

    @Volatile
    var y: Float = 0f

    @Volatile
    var eyex: Float = 0f

    @Volatile
    var eyey: Float = 0f

    @Volatile
    var eyez: Float = 37f

    @Volatile
    var centerX: Float = 0f

    @Volatile
    var centerY: Float = 0f

    @Volatile
    var centerZ: Float = 0f

    @Volatile
    var tranlate: Float = 1f

    @Volatile
    var target: Point = Point(1f, 1f)
    @Volatile
    var isMove:Boolean = false

    //
    private lateinit var mDemo: Demo
    private lateinit var mPerson: Person
    private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val context: Context = context

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 0.5f)
        // initialize a triangle
        mDemo = Demo(context, 1703f, 586f, 8f, "c7Map", "png")
        mSquare = Square(floatArrayOf(1f,1f,1f,1f))
        mPerson = Person(context, "navigation", "png")
        unused.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, eyex, eyey, eyez, centerX, centerY, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        mDemo.draw(vPMatrix)
//        Matrix.setIdentityM(mPerson.mModelMatrix, 0);
//        Matrix.translateM(mPerson.mModelMatrix,0,0.01f,0f,0f)
//        Matrix.setRotateM(rotationMatrix, 0, 0f, 0f, 0f, -1f);
//        Matrix.multiplyMM(mPerson.mModelMatrix, 0, vPMatrix, 0, rotationMatrix, 0)
        mPerson.draw(vPMatrix, target.x, target.y,angle)
        mSquare.draw(vPMatrix,1f)

    }
    private fun caculatePointByDistanceAndAngle(point: Point, distance:Float, angle:Float): Point {
        val x1 = point.x + distance * sin(angleToRadians(angle));
        val y1 = point.y + distance * cos(angleToRadians(angle));
        return Point(x1,y1)
    }
    private fun angleToRadians(value:Float):Float {
        return ((value / 360) * 2 * PI).toFloat()
    }
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 200f)
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                // add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }

}