package com.example.beaconscanner.model.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.beaconscanner.model.Point;
import com.example.beaconscanner.utils.ColorUntil;
import com.example.beaconscanner.utils.CoordinateCaculator;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Square {
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    public float[] mModelMatrix = new float[16];
    static final int COORDS_PER_VERTEX = 3;
    public float size;
    float squareCoords[]; // top right
    private final short drawOrder[] = {0, 1, 2,
            0, 2, 3,
            0, 4, 1,
            1, 4, 2,
            2, 4, 3,
            3, 4, 0
    }; // order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};
    private Point A;
    private Point B;
    private Point C;
    private Point D;
    private Point O = new Point(0f, 0f);
    private Point middle = new Point(0f,0f);

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Square(float x, float y, float size,float[] color) {
        this.color = color;
        middle.setX(x);
        middle.setY(y);
        this.size = size;
        Point middlePoint = new Point(x, y);
        float R = (float) (size * Math.sqrt(2)) / 2;
        A = new Point(x - this.size / 2, y + this.size / 2);
        B = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f);
        C = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f - 90);
        D = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f - 180);
        squareCoords = new float[]{
                //x,    y
//                // square
                A.getX(), A.getY(), 0f,// top left
                B.getX(), B.getY(), 0f,// bottom left
                C.getX(), C.getY(), 0f,// bottom right
                D.getX(), D.getY(), 0f,// top right
        };
        Matrix.setIdentityM(mModelMatrix, 0);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        // prepare shaders and OpenGL program
        int vertexShader = loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void setPositionByCoordinate(Point coordinate) {
        middle = coordinate;
        float R = (float) (size * Math.sqrt(2)) / 2;
        A = new Point(middle.getX() - this.size / 2, middle.getY() + this.size / 2);
        B = CoordinateCaculator.INSTANCE.circleXY(middle, R, 225f);
        C = CoordinateCaculator.INSTANCE.circleXY(middle, R, 225f - 90);
        D = CoordinateCaculator.INSTANCE.circleXY(middle, R, 225f - 180);
        squareCoords = new float[]{
                //x,    y
//                // square
                A.getX(), A.getY(), 0f,// top left
                B.getX(), B.getY(), 0f,// bottom left
                C.getX(), C.getY(), 0f,// bottom right
                D.getX(), D.getY(), 0f,// top right
        };
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    public Point getPosition() {
        return middle;
    }

    public void setColor(float[] color) {
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");
        GLES20.glLineWidth(5f);
        int position = 0;
        // Draw the square
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 0, 4);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
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