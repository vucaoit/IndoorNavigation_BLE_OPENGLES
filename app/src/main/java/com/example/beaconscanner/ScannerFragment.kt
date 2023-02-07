package com.example.beaconscanner

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.beaconscanner.components.JoystickView
import com.example.beaconscanner.databinding.FragmentScannerBinding
import com.example.beaconscanner.model.Point
import com.example.beaconscanner.utils.CoordinateCaculator
import com.example.beaconscanner.utils.Utils
import java.util.*
import kotlin.math.*


class ScannerFragment : Fragment(), SensorEventListener {
    private lateinit var binding : FragmentScannerBinding
    private var max = 0;
    private var min = 0;
    private var degreePrevous = 0f
    private lateinit var sensorManager: SensorManager
    private var angle = 0f
    private var distances = arrayListOf<Float>()
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
    private var x = 0f
    private var y = 0f
    val eddystoneServiceId: ParcelUuid =
        ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    private var isStart = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScannerBinding.inflate(inflater,container,false);
        initViews()
        // 1
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
// 2
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
// 3
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        setUpBluetoothManager()
        return binding.root
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this);
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        );
    }

    private fun initViews() {
        binding.joyStick.setOnJoystickMoveListener({ angle, _, _ ->
            binding.myGLSurfaceView.apply {
                setAngle(angle.toFloat())
                setTarget(angle.toFloat(),0.05f)
                requestRender()
            }
//            Log.d("ANGLE","$angle")
        }, JoystickView.DEFAULT_LOOP_INTERVAL)
    }
    private fun onStartScannerButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        } else {
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {

            btScanner?.startScan(leScanCallback)
        }
    }

    private fun onStopScannerButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        btScanner!!.stopScan(leScanCallback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpBluetoothManager() {
        btManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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
        if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect  peripherals.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_COARSE_LOCATION
                )
            }
            builder.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(activity)
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
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            beacon.manufacturer = result.device.name
            beacon.rssi = result.rssi
            if (scanRecord != null) {
                val serviceUuids = scanRecord.serviceUuids
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)

                if (serviceUuids != null && serviceUuids.size > 0 && serviceUuids.contains(
                        eddystoneServiceId
                    )
                ) {
                    val serviceData = scanRecord.getServiceData(eddystoneServiceId)

                    if (serviceData != null && serviceData.size >= 18) {
                        val eddystoneUUID =
                            Utils.toHexString(Arrays.copyOfRange(serviceData, 2, 18))
                        val namespace = String(eddystoneUUID.toCharArray().sliceArray(0..19))
                        val instance = String(
                            eddystoneUUID.toCharArray()
                                .sliceArray(20 until eddystoneUUID.toCharArray().size)
                        )
                        beacon.type = Beacon.beaconType.eddystoneUID
                        beacon.namespace = namespace
                        beacon.instance = instance

//                        Log.e("EddyStone", "Namespace:$namespace Instance:$instance")
                    }
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
//                    Log.e(
//                        "IBeacon",
//                        "iBeaconUUID:$iBeaconUUID major:$major minor:$minor rssi:${beacon.rssi}, distance : "
//                    )
                    if (iBeaconUUID == "FDA50693A4E24FB1AFCFC6EB07647825") {
                        val MeasurePower = -69
                        val N = 2
                        val x = (10.0.pow((MeasurePower - beacon.rssi!!) / (10.0 * N)))
                        if (distances.size > 2) {
                            var avg = 0.0f;
                            for (item in distances) {
                                avg += item
                            }
                            avg /= distances.size
                            binding.apply {
                                myGLSurfaceView.setTranlate(avg)
                                myGLSurfaceView.requestRender()
//                                distance.text = "Distance : $x \nRssi : ${beacon.rssi!!}"
                            }
//                            Log.e("DISTANCE","$avg")
                            distances.clear()

                        } else {
                            distances.add(x.toFloat())
                        }

//                        Log.e(
//                            "IBeacon",
//                            "iBeaconUUID:$iBeaconUUID major:$major minor:$minor rssi:${beacon.rssi}, distance : ${x}"
//                        )

                        if (min == 0 || max == 0) {
                            min = beacon.rssi!!
                            max = beacon.rssi!!
                        }
                        if (beacon.rssi!! < min) min = beacon.rssi!!
                        if (beacon.rssi!! > max) max = beacon.rssi!!
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("DINKAR", errorCode.toString())
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val degree = event!!.values[0].roundToInt().toFloat() + 95f
        if (degree > 0 && abs((degreePrevous - degree)) < 20) {
//            Log.d("Degree", "$degree")
////            angle = degree
//            binding.apply {
//                myGLSurfaceView.setAngle(degree)
////            myGLSurfaceView.setTargetCoordinate(getPointByDistanceAndAngle(Point(1f,1f),0.001f,degree))
//                myGLSurfaceView.requestRender()
//            }
        }
        degreePrevous = degree
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}