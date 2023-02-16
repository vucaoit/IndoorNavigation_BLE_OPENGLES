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
public class LangTru {
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

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public LangTru(float x, float y, float size) {
        this.size = size;
        Point middlePoint = new Point(x, y);
        float R = (float) (size * Math.sqrt(2)) / 2;
        A = new Point(x - this.size / 2, y + this.size / 2);
        B = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f);
        C = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f - 90);
        D = CoordinateCaculator.INSTANCE.circleXY(middlePoint, R, 225f - 180);
        System.out.println(A.toString());
        System.out.println(B.toString());
        System.out.println(C.toString());
        System.out.println(D.toString());
        squareCoords = new float[]{
                //x,    y
//                // square
                -0.5f, 0.5f, 0f,// top left
                -0.5f, -0.5f, 0f,// bottom left
                0.5f, -0.5f, 0f,// bottom right
                0.5f,0.5f, 0f,// top right
                //back
                -0.5f,0.5f,0f,
                0.5f,0.5f,0f,
                0f,0f,1f,
                //right
                0.5f,0.5f,0f,
                0.5f,-0.5f,0f,
                0f,0f,1f,
                //back
                0.5f,-0.5f,0f,
                -0.5f,-0.5f,0f,
                0f,0f,1f,
                //left
                -0.5f,-0.5f,0f,
                -0.5f,0.5f,0f,
                0f,0f,1f,

                //draw border

                //bottom
                -0.5f, 0.5f, 0f,// top left
                -0.5f, -0.5f, 0f,// bottom left
                0.5f, -0.5f, 0f,// bottom right
                0.5f,0.5f, 0f,// top right
                //back
                -0.5f, 0.5f, 0f,
                0.5f,0.5f, 0f,
                0.5f,0.5f, 1f,
                -0.5f, 0.5f, 1f,
                //right
                0.5f,0.5f,0f,
                0.5f,-0.5f,0f,
                0.5f,-0.5f,1f,
                0.5f,0.5f,1f,
                //font
                0.5f,-0.5f,0f,
                -0.5f,-0.5f,0f,
                -0.5f,-0.5f,1f,
                0.5f,-0.5f,1f,
                //left
                -0.5f,-0.5f,0f,
                -0.5f,0.5f,0f,
                -0.5f,0.5f,1f,
                -0.5f,-0.5f,1f,
                //top
                -0.5f, 0.5f, 1f,// top left
                -0.5f, -0.5f, 1f,// bottom left
                0.5f, -0.5f, 1f,// bottom right
                0.5f,0.5f, 1f,// top right

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

    public void setPositionByRootCoordinate(Point rootCoordinate) {
        squareCoords = new float[]{
                //x,    y
                A.getX() + rootCoordinate.getX(), A.getY() + rootCoordinate.getY(), 0f,// top left
                B.getX() + rootCoordinate.getX(), B.getY() + rootCoordinate.getY(), 0f,// bottom left
                C.getX() + rootCoordinate.getX(), C.getY() + rootCoordinate.getY(), 0f,// bottom right
                D.getX() + rootCoordinate.getX(), D.getY() + rootCoordinate.getY(), 0f,// top right
        };
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }
    public void setColor(float[] color){
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
        setColor(ColorUntil.INSTANCE.getRED());
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 0, 4 );

        setColor(ColorUntil.INSTANCE.getBLUE());
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 4, 3 );

        setColor(ColorUntil.INSTANCE.getORANGE());
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 7, 3 );
        setColor(ColorUntil.INSTANCE.getGREEN());
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 9, 3 );
        setColor(ColorUntil.INSTANCE.getYELLOW());
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_FAN, 12, 3 );

        //draw border
        setColor(ColorUntil.INSTANCE.getWHITE());
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 16, 4 );
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 20, 4 );
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 24, 4 );
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 28, 4 );
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 32, 4 );
        GLES20.glDrawArrays(
                GLES20.GL_LINE_LOOP, 36, 4 );
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