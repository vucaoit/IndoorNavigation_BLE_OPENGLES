package com.example.beaconscanner.views.opengl

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.beaconscanner.components.JoystickView
import com.example.beaconscanner.controller.ToolController
import com.example.beaconscanner.databinding.ActivityOpenglBinding
import com.example.beaconscanner.model.Beacon
import com.example.beaconscanner.utils.CoordinateCaculator
import com.example.beaconscanner.utils.Utils
import com.google.android.material.slider.Slider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class OpenglActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenglBinding
    private var isBeacon1Clicked = false
    private var isBeacon2Clicked = false
    private var isBeacon3Clicked = false
    private var speed = 0.05f
    private var max = 0;
    private var min = 0;
    private val beacon1 = "FDA50693A4E24FB1AFCFC6EB07647821"
    private val beacon2 = "FDA50693A4E24FB1AFCFC6EB07647822"
    private val beacon3 = "FDA50693A4E24FB1AFCFC6EB07647823"
    private var timeStart: Calendar? = null
    private var isStart = false
    private var arrRssi1 = ArrayList<Float>()
    private var arrRssi2 = ArrayList<Float>()
    private var arrRssi3 = ArrayList<Float>()
    private val toolController = ToolController()
    var N = 3f
    var measuaredPower = -58

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 1) {
                //granted
                println("cap phep")
            } else {
                //deny
                println("tu choi")
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }
    }
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }
    private var btManager: BluetoothManager? = null
    private var btAdapter: BluetoothAdapter? = null
    private var btScanner: BluetoothLeScanner? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenglBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideAllTools()
        //tool selected
        binding.apply {
            btnToolBeacon.setOnClickListener {
                hideAllTools()
                toolController.mode = ToolController.MOVE_BEACON
                myGLSurfaceView.setMode(ToolController.MOVE_BEACON)
                lnLayoutSetupBeacon.visibility = View.VISIBLE
                joyStick.visibility = View.VISIBLE
            }
            btnToolAdjust.setOnClickListener {
                hideAllTools()
                toolController.mode = ToolController.ADJUST_BEACON
                myGLSurfaceView.setMode(ToolController.ADJUST_BEACON)
                lnLayoutAdjust.visibility = View.VISIBLE
                joyStick.visibility = View.GONE
            }
            btnToolMap.setOnClickListener {
                hideAllTools()
                toolController.mode = ToolController.DRAW_MAP
                myGLSurfaceView.setMode(ToolController.DRAW_MAP)
                lnLayoutDrawMap.visibility = View.VISIBLE
                joyStick.visibility = View.VISIBLE
            }
            btnToolMoveAndZoom.setOnClickListener {
                hideAllTools()
                toolController.mode = ToolController.MOVE_AND_ZOOM
                myGLSurfaceView.setMode(ToolController.MOVE_AND_ZOOM)
                lnLayoutMoveAndZoom.visibility = View.VISIBLE
                joyStick.visibility = View.VISIBLE
            }
            btnToolCancel.setOnClickListener {
                hideAllTools()
            }
        }
        //draw map controller
        binding.apply {
            btnMapPin.setOnClickListener {
                myGLSurfaceView.openGLRender.apply {
                    if (map.points.size < 1) {
                        map.addPoint(drawer.position)
                        map.addPoint(drawer.position)
                    } else {
                        map.addPoint(drawer.position)
                    }
                }
            }
            btnMapUndo.setOnClickListener {
                myGLSurfaceView.openGLRender.apply {
                    map.removeLastPoint()
                    map.points[map.points.size - 1] = drawer.position
                    map.resetMap()
                }
            }
        }
        //beacon mode selected
        binding.apply {
            btnBeacon1.setOnClickListener {
                isBeacon1Clicked = true
                isBeacon2Clicked = false
                isBeacon3Clicked = false
            }
            btnBeacon2.setOnClickListener {
                isBeacon1Clicked = false
                isBeacon2Clicked = true
                isBeacon3Clicked = false
            }
            btnBeacon3.setOnClickListener {
                isBeacon1Clicked = false
                isBeacon2Clicked = false
                isBeacon3Clicked = true
            }
            btnStart.setOnClickListener {
                if (!isStart) {
                    onStartScannerButtonClick()
                    btnStart.text = "STOP"
                } else {
                    onStopScannerButtonClick()
                    btnStart.text = "Start"
                }
                isStart = !isStart
                myGLSurfaceView.openGLRender.beaconStart = isStart
            }
        }
        //zoom mode
        binding.apply {

        }
        binding.apply {
            joyStick.setOnJoystickMoveListener({ angle, power, _ ->
                speed = (0.0001f * power)/ myGLSurfaceView.openGLRender.zoom
                when (toolController.mode) {
                    ToolController.DRAW_MAP -> {
                        myGLSurfaceView.openGLRender.apply {
                            if (map.points.size > 1) {
                                map.points[map.points.size - 1] = drawer.position
                                map.resetMap()
                                drawer.apply {
                                    setPositionByCoordinate(
                                        CoordinateCaculator.circleXY(
                                            position,
                                            speed,
                                            angle * 1.0f
                                        )
                                    )
                                }
                            } else {
                                drawer.apply {
                                    setPositionByCoordinate(
                                        CoordinateCaculator.circleXY(
                                            position,
                                            speed,
                                            angle * 1.0f
                                        )
                                    )
                                }
                            }
                        }
                    }
                    ToolController.MOVE_BEACON -> {
                        if (isBeacon1Clicked) {
                            val position = myGLSurfaceView.beacon1GetPosition()
                            val newPosition =
                                CoordinateCaculator.circleXY(position, speed, angle * 1.0f)
                            myGLSurfaceView.beacon1SetPosition(newPosition)
                            println(newPosition)
                        } else {
                            if (isBeacon2Clicked) {
                                val position = myGLSurfaceView.beacon2GetPosition()
                                val newPosition =
                                    CoordinateCaculator.circleXY(position, speed, angle * 1.0f)
                                myGLSurfaceView.beacon2SetPosition(newPosition)
                                println(newPosition)
                            } else {
                                val position = myGLSurfaceView.beacon3GetPosition()
                                val newPosition =
                                    CoordinateCaculator.circleXY(position, speed, angle * 1.0f)
                                myGLSurfaceView.beacon3SetPosition(newPosition)
                                println(newPosition)
                            }
                        }
                    }
                    ToolController.MOVE_AND_ZOOM -> {
                        myGLSurfaceView.openGLRender.apply {
                            var alpha = angle
                            when(angle){
                                in 1..179->{
                                    alpha = -(180-angle)
                                }
                                in -179..-1->{
                                    alpha = (angle+180)
                                }
                            }
                            tranlateCoordinate =
                                CoordinateCaculator.circleXY(tranlateCoordinate, speed / 0.1f, alpha * 1f)
                        }
                    }
                    else -> {
                    }
                }
            }, JoystickView.DEFAULT_LOOP_INTERVAL)
            sliderMp.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
                measuaredPower = value.roundToInt()
            })
            sliderN.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
                N = value
            })
            sliderZoom.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
                if (value < 5) {
                    myGLSurfaceView.openGLRender.zoom = value
                }
            })
        }
        setUpBluetoothManager()

    }

    private fun hideAllTools() {
        binding.apply {
            lnLayoutSetupBeacon.visibility = View.GONE
            lnLayoutDrawMap.visibility = View.GONE
            lnLayoutAdjust.visibility = View.GONE
            lnLayoutMoveAndZoom.visibility = View.GONE
            joyStick.visibility = View.GONE
        }
    }

    private fun onStartScannerButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        } else {
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
//            timeStart = Calendar.getInstance()
            btScanner?.startScan(leScanCallback)
        }
    }

    private fun onStopScannerButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        btScanner!!.stopScan(leScanCallback)
        timeStart = null
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpBluetoothManager() {
        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager!!.adapter
        btScanner = btAdapter?.bluetoothLeScanner
        if (btAdapter != null && !btAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
        checkForLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermission() {
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect  peripherals.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_COARSE_LOCATION,
                )
            }
            builder.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover BLE beacons")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scanRecord = result.scanRecord
            val beacon = Beacon(result.device.address)
            if (ActivityCompat.checkSelfPermission(
                    this@OpenglActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            beacon.manufacturer = result.device.name
            beacon.rssi = result.rssi
            if (scanRecord != null) {
                var iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
                if (iBeaconManufactureData == null) {
                    iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0XFFFF)
                }
                if (iBeaconManufactureData != null && iBeaconManufactureData.size >= 23) {
                    val iBeaconUUID = Utils.toHexString(iBeaconManufactureData.copyOfRange(2, 18))
                    val major = Integer.parseInt(
                        Utils.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                18,
                                20
                            )
                        ), 16
                    )
                    val minor = Integer.parseInt(
                        Utils.toHexString(
                            iBeaconManufactureData.copyOfRange(
                                20,
                                22
                            )
                        ), 16
                    )
                    beacon.raw = Utils.toHexString(iBeaconManufactureData)
                    beacon.type = Beacon.beaconType.iBeacon
                    beacon.uuid = iBeaconUUID
                    beacon.major = major
                    beacon.minor = minor
                    when (iBeaconUUID) {
                        beacon1 -> {

                            if (arrRssi1.size < 3) {
                                arrRssi1.add(beacon.rssi!! * 1f)
                            } else {
                                val temp = (arrRssi1[0] + arrRssi1[1] + arrRssi1[2]) / 3
                                val distance =
                                    Math.pow(10.0, (measuaredPower - temp) / (10.0 * N))
                                binding.myGLSurfaceView.setDistance1(distance.toFloat())
                                arrRssi1.removeAt(0)
                            }
//                            Log.d("BEACON1",arrRssi1.toString())
                        }
                        beacon2 -> {
                            if (arrRssi2.size < 3) {
                                arrRssi2.add(beacon.rssi!! * 1f)
                            } else {
                                val temp = (arrRssi2[0] + arrRssi2[1] + arrRssi2[2]) / 3
                                val distance =
                                    Math.pow(10.0, (measuaredPower - temp) / (10.0 * N))
                                binding.myGLSurfaceView.setDistance2(distance.toFloat())
                                arrRssi2.removeAt(0)
                            }
//                            Log.d("BEACON2",arrRssi2.toString())
                        }
                        beacon3 -> {
                            if (arrRssi3.size < 3) {
                                arrRssi3.add(beacon.rssi!! * 1f)
                            } else {
                                val temp = (arrRssi3[0] + arrRssi3[1] + arrRssi3[2]) / 3
                                val distance =
                                    Math.pow(10.0, (measuaredPower - temp) / (10.0 * N))
                                binding.myGLSurfaceView.setDistance3(distance.toFloat())
                                arrRssi3.removeAt(0)
                            }
//                            Log.d("BEACON3",arrRssi3.toString())
                        }
                    }


                    if (min == 0 || max == 0) {
                        min = beacon.rssi!!
                        max = beacon.rssi!!
                    }
                    if (beacon.rssi!! < min) min = beacon.rssi!!
                    if (beacon.rssi!! > max) max = beacon.rssi!!
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("DINKAR", errorCode.toString())
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        private const val RATIO_DISTANCE = 0.14f
    }
}