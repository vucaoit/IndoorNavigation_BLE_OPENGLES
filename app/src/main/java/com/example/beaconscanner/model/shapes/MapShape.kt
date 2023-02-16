package com.example.beaconscanner.model.shapes

import android.opengl.GLES20
import android.util.Log
import com.example.beaconscanner.model.Point
import com.example.beaconscanner.utils.ColorUntil.BLACK_03
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
class MapShape{
    private var VertexBuffer: FloatBuffer
    private val VertexShaderCode =  // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +  // the matrix must be included as a modifier of gl_Position
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"
    private val FragmentShaderCode = ("precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}")
    private var GlProgram: Int
    private var PositionHandle = 0
    private var ColorHandle = 0
    private var MVPMatrixHandle = 0
    var points = ArrayList<Point>()
    private var LineCoords: FloatArray = floatArrayOf()
    private var VertexCount = 0
    private val VertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    var color = BLACK_03
    init {
        VertexCount = LineCoords.size / COORDS_PER_VERTEX
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect( // (number of coordinate values * 4 bytes per float)
            LineCoords.size * 4
        )
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        VertexBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        VertexBuffer.put(LineCoords)
        // set the buffer to read the first coordinate
        VertexBuffer.position(0)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode)
        GlProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        GLES20.glAttachShader(GlProgram, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(GlProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(GlProgram) // creates OpenGL ES program executables
    }
    fun addPoint(point: Point){
        points.add(point)
        println(points)
        resetMap()
    }
    fun removeLastPoint(){
        points.removeLast()
        resetMap()
    }
    fun resetMap(){
        if(points.size>0){
            val temp = FloatArray(points.size * 3)
            for(i in 0 until points.size){
                temp[i*3 + 0] = points[i].x
                temp[i*3 + 1] = points[i].y
                temp[i*3 + 2] = 0f
            }
            LineCoords = temp
            VertexCount = LineCoords.size / COORDS_PER_VERTEX
            val bb = ByteBuffer.allocateDirect(LineCoords.size * 4)
            bb.order(ByteOrder.nativeOrder())
            VertexBuffer = bb.asFloatBuffer()
            VertexBuffer.put(LineCoords)
            VertexBuffer.position(0)
        }
    }
    fun draw(mvpMatrix: FloatArray?) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(GlProgram)
        // get handle to vertex shader's vPosition member
        PositionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition")
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(PositionHandle)
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            PositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            VertexStride, VertexBuffer
        )
        // get handle to fragment shader's vColor member
        ColorHandle = GLES20.glGetUniformLocation(GlProgram, "vColor")
        // Set color for drawing the triangle
        GLES20.glUniform4fv(ColorHandle, 1, color, 0)
        // get handle to shape's transformation matrix
        MVPMatrixHandle = GLES20.glGetUniformLocation(GlProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation")
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")
        // Draw the triangle
        GLES20.glLineWidth(5f)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, VertexCount)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(PositionHandle)
    }

    companion object {
        val COORDS_PER_VERTEX = 3
        fun checkGlError(glOperation: String) {
            var error: Int
            while ((GLES20.glGetError().also { error = it }) != GLES20.GL_NO_ERROR) {
                Log.e("AHIHIH", "$glOperation: glError $error")
                throw RuntimeException("$glOperation: glError $error")
            }
        }

        fun loadShader(type: Int, shaderCode: String?): Int {
            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
    }
}