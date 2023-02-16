package com.example.beaconscanner.views.opengl.temp//package com.example.beaconscanner.views.opengl//package com.example.beaconscanner.opengl
//
//import android.app.Person
//import android.content.Context
//import android.opengl.GLES20
//import android.opengl.GLSurfaceView
//import android.opengl.Matrix
//import com.example.beaconscanner.model.*
//import com.example.beaconscanner.model.shapes.Demo
//import com.example.beaconscanner.model.shapes.Line
//import com.example.beaconscanner.model.shapes.Person
//import com.example.beaconscanner.model.shapes.Square
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//
//class MyGLRenderer(context: Context) : GLSurfaceView.Renderer {
//    @Volatile
//    var angle: Float = 5f
//
//    @Volatile
//    var x: Float = 0f
//
//    @Volatile
//    var y: Float = 0f
//
//    @Volatile
//    var eyex: Float = 0f
//
//    @Volatile
//    var eyey: Float = 0f
//
//    @Volatile
//    var eyez: Float = 50f
//
//    @Volatile
//    var centerX: Float = 0f
//
//    @Volatile
//    var centerY: Float = 0f
//
//    @Volatile
//    var centerZ: Float = 0f
//
//    @Volatile
//    var tranlate: Float = 1f
//
//    @Volatile
//    var target: Point = Point(1f, 1f)
//
//    @Volatile
//    var zoom: Float = 3f
//
//    @Volatile
//    var isMove: Boolean = false
//
//    @Volatile
//    var ratio: Float = 0f
//
//    //
//    private lateinit var mDemo: Demo
//    private lateinit var mPerson: Person
//    private var listPerson: ArrayList<Person> = arrayListOf()
//    private lateinit var mSquare: Square
//    private lateinit var lineDemo: Line
////    private lateinit var mtriagle:Triangle
//
//    // vPMatrix is an abbreviation for "Model View Projection Matrix"
//    private val vPMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)
//    private val context: Context = context
//
//    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
//        // Set the background frame color
//        GLES20.glClearColor(1f, 1f, 1f, 0.5f)
//        // initialize a model
//        mDemo = Demo(context, 1703f, 586f, 8f, "c7Map", "png")
//        mSquare = Square(
//            floatArrayOf(
//                1f,
//                1f,
//                1f,
//                1f
//            )
//        )
//        mPerson = Person(context, "navigation", "png")
//        lineDemo = Line(1f, 2,100f)
//        for (item in 1..5) {
//            listPerson.add(Person(context, "navigation", "png"))
//        }
//        unused.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//    }
//
//    override fun onDrawFrame(unused: GL10) {
//        // Redraw background color
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, zoom, 200f)
//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(viewMatrix, 0, eyex, eyey, eyez, centerX, centerY, 0f, 0f, 1f, 0f)
//        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//        mDemo.draw(vPMatrix)
////        mPerson.draw(vPMatrix, target.x, target.y,angle)
//        for (item in listPerson) {
//            item.draw(vPMatrix, target.x, target.y, listPerson.indexOf(item) * 72f)
//        }
//        mSquare.draw(vPMatrix, 1f)
//        lineDemo.draw(vPMatrix)
//
////        mtriagle.draw(vPMatrix)
//
//    }
//
//    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
//        GLES20.glViewport(0, 0, width, height)
//
//        ratio = width.toFloat() / height.toFloat()
//        // this projection matrix is applied to object coordinates
//        // in the onDrawFrame() method
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, angle, 200f)
//    }
//
//    companion object {
//        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
//        fun loadShader(type: Int, shaderCode: String): Int {
//
//            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
//            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
//            return GLES20.glCreateShader(type).also { shader ->
//
//                // add the source code to the shader and compile it
//                GLES20.glShaderSource(shader, shaderCode)
//                GLES20.glCompileShader(shader)
//            }
//        }
//    }
//
//}