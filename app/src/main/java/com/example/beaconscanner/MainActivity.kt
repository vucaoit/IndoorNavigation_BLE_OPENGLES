package com.example.beaconscanner

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadScannerFragment()
    }

    private fun loadScannerFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.your_placeholder, ScannerFragment())
        ft.commit()
    }
}