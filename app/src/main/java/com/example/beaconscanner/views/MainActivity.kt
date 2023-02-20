package com.example.beaconscanner.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.beaconscanner.databinding.ActivityMainBinding
import com.example.beaconscanner.views.beacon.BeaconScannerActivity
import com.example.beaconscanner.views.opengl.OpenglActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.activityOpengl.setOnClickListener{
            startActivity(Intent(this,OpenglActivity::class.java))
//        }
//        binding.activityBeaconScanner.setOnClickListener{
//            startActivity(Intent(this,BeaconScannerActivity::class.java))
//        }
    }
}