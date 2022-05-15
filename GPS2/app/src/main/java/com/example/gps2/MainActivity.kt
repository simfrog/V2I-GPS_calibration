package com.example.gps2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private var time: Long? = null
    private var location: TextView? = null
    private var lastLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        location = findViewById(R.id.location)
    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSIONS)
            } else {
                initLocation()
            }
        } else {
            initLocation()
        }
    }

    private fun initLocation() {
        val TAG:String = "initlocation : "

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        var currentTime: Long? = null

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?: return
                    for(loc in locationResult.locations) {
                        currentTime = System.currentTimeMillis()
                        Log.d(TAG, "#${currentTime} ${loc.latitude} , ${loc.longitude}")
                        lastLocation = loc
                        time = currentTime
                        printLocation()
                    }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun printLocation() {
        location?.text = "Time:${time}\n Location latitude:${lastLocation?.latitude}\n\tlongitude:${lastLocation?.longitude}"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSIONS -> if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation()
            }
        }
    }

    companion object {
        private val REQUEST_LOCATION_PERMISSIONS = 1
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
