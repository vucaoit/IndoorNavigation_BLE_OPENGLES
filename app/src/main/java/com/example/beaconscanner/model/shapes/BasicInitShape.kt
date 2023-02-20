package com.example.beaconscanner.model.shapes

import android.opengl.GLES20

open class BasicInitShape {
     val vertexShaderCode =  //Test
        "attribute vec2 a_TexCoordinate;" +
                "varying vec2 v_TexCoordinate;" +  //End Test
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition * uMVPMatrix;" +  //Test
                "v_TexCoordinate = a_TexCoordinate;" +  //End Test
                "}"

    val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
    fun loadShader(type: Int, shaderCode: String?): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    companion object {



    }
}