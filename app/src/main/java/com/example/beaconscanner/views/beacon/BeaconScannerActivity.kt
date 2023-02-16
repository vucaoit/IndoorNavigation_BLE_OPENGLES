package com.example.beaconscanner.views.beacon

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
import android.os.Bundle
import android.os.Environment
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.beaconscanner.databinding.ActivityBeaconScannerBinding
import com.example.beaconscanner.model.Beacon
import com.example.beaconscanner.utils.Utils
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class BeaconScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBeaconScannerBinding
    private var isStart = false;
    private var max = 0;
    private var min = 0;
    private val beacon1 = "FDA50693A4E24FB1AFCFC6EB07647825"
    private var timeStart: Calendar? = null
    private var isWriting = false
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
    val eddystoneServiceId: ParcelUuid =
        ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeaconScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.apply {
            btnScanner.setOnClickListener {
                if (!isStart) {
                    btnScanner.text = "STOP"
                    onStartScannerButtonClick()

                } else {
                    btnScanner.text = "START"
                    onStopScannerButtonClick()
                }
                isStart = !isStart
            }
            btnCatchData.setOnClickListener {
                if (!isWriting) {
                    Toast.makeText(applicationContext, "catching data", Toast.LENGTH_SHORT).show()
                    btnCatchData.text = "Recording"
                    timeStart = Calendar.getInstance()
                } else {
                    Toast.makeText(applicationContext, "saved", Toast.LENGTH_SHORT).show()
                    btnCatchData.text = "Catch"
                    timeStart = null
                }
                isWriting = !isWriting
            }
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
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE),
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
                    this@BeaconScannerActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            beacon.manufacturer = result.device.name
            beacon.rssi = result.rssi
            if (scanRecord != null) {
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
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
                    if (iBeaconUUID == beacon1 && major == 10010) {
                        Log.d("RSSI", "${beacon.rssi}")
                        val fileName = "BeaconTesting"
                        if (isWriting) {
                            writeFile(
                                fileName,
                                "" + binding.edtMetter.text + "," + binding.edtTxpower.text + "," + binding.edtAdvinterval.text + "," + countTime() + "," + beacon.toString(),
                                true
                            )
                        }
                        val measuaredPower = -57
                        val N = 4.0
                        val distance =
                            Math.pow(10.0, (measuaredPower - beacon.rssi!!) / (10.0 * N)).format(2)
                        Log.d("DISTANCE", "$distance")
                        binding.txtBeacnInfo.text =
                            "time : ${countTime()}\nmacAdd : ${beacon.macAddress}\nuuid : ${beacon.uuid}\nmajor : ${beacon.major}\nminor : ${beacon.minor}\nrssi : ${beacon.rssi}\ndistance : ${distance}"
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

        fun Double.format(digits: Int) = "%.${digits}f".format(this)
        fun countTime(): String {
            if (timeStart != null) {
                val time = Calendar.getInstance().timeInMillis - timeStart!!.timeInMillis
                return "${time / 1000}s${formatNumber((time % 1000).toInt())}"
            }
            return "0s0"
        }

        fun formatNumber(number: Int): String {
            return if (number < 10) {
                "00${number}"
            } else {
                if (number >= 10 && number < 100) {
                    "0${number}"
                } else {
                    number.toString()
                }
            }
        }

        fun writeFile(fileName: String, fileData: String, append: Boolean) {

            val file = File(filesDir, "/${fileName}.txt")

            var fos: FileOutputStream? = null

            try {
                fos = FileOutputStream(file, append)

                // Writes bytes from the specified byte array to this file output stream
                fos.write(fileData.toByteArray())
            } catch (e: FileNotFoundException) {
                println("File not found$e")
            } catch (ioe: IOException) {
                println("Exception while writing file $ioe")
            } finally {
                // close the streams using close method
                try {
                    fos?.close()
                } catch (ioe: IOException) {
                    println("Error while closing stream: $ioe")
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
    }
}