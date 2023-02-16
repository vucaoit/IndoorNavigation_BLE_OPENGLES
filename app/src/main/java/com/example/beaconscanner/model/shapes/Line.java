package com.example.beaconscanner.model.shapes;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.beaconscanner.ShaderUtil;
import com.example.beaconscanner.model.Point;
import com.example.beaconscanner.utils.ColorUntil;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Line {
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    private FloatBuffer VertexBuffer;
    private final String VertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +

                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String FragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    protected int GlProgram;
    protected int PositionHandle;
    protected int ColorHandle;
    protected int MVPMatrixHandle;
    private Point A;
    private Point B;
    private float x;
    private float y;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float LineCoords[];

    private int VertexCount = 0;
    private final int VertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = ColorUntil.INSTANCE.getBLACK_03();
    private int orientation = 0;
    public void setColor(float[] color){
        this.color = color;
    }
    public void setPositionByRootCoordinate(Point rootCoordinate) {
        LineCoords = new float[]{
                A.getX() + rootCoordinate.getX(), A.getY() + rootCoordinate.getY(), 0.0f,
                B.getX() + rootCoordinate.getY(), B.getY() + rootCoordinate.getY(), 0.0f
        };

        VertexCount = LineCoords.length / COORDS_PER_VERTEX;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                LineCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        VertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        VertexBuffer.put(LineCoords);
        // set the buffer to read the first coordinate
        VertexBuffer.position(0);
    }

    public Line(float index, int orientation1, float length) {
        orientation = orientation1;
        if (orientation == VERTICAL) {
            A = new Point(index, -length);
            B = new Point(index, length);
//            LineCoords = new float[]{
//                    index + wordCoordinate.getX(), -length + wordCoordinate.getY(), 0.0f,
//                    index + wordCoordinate.getX(), length + wordCoordinate.getY(), 0.0f
//            };
        } else {
            A = new Point(-length, index);
            B = new Point(length, index);
        }
        LineCoords = new float[]{
                A.getX(), A.getY(), 0.0f,
                B.getX(), B.getY(), 0.0f
        };

        VertexCount = LineCoords.length / COORDS_PER_VERTEX;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                LineCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        VertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        VertexBuffer.put(LineCoords);
        // set the buffer to read the first coordinate
        VertexBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FragmentShaderCode);

        GlProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(GlProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(GlProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(GlProgram);                  // creates OpenGL ES program executables
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(GlProgram);
        // get handle to vertex shader's vPosition member
        PositionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(PositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(PositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VertexStride, VertexBuffer);
        // get handle to fragment shader's vColor member
        ColorHandle = GLES20.glGetUniformLocation(GlProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(ColorHandle, 1, color, 0);
        // get handle to shape's transformation matrix
        MVPMatrixHandle = GLES20.glGetUniformLocation(GlProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");
        // Draw the triangle
        GLES20.glLineWidth(0.01f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, VertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(PositionHandle);
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("AHIHIH", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}