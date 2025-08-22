package com.route.todoappc42

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.route.todoappc42.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityLocationBinding
    lateinit var locationPermissionRequest: ActivityResultLauncher<String>
    lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpMap()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        registerActivityForLocationResult()
        if (isLocationPermissionGranted()) {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (isGpsEnabled) {
                accessLocation()
            } else {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Gps is disabled")
                    .setMessage("Gps access is required to attach task to specific location")
                    .setPositiveButton("ok") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                alertDialog.show()

            }
        } else {
            if (!shouldShowRationale()) {
                requestLocationPermission()
            }

        }

    }

    private fun setUpMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun accessLocation() {
        var currentLocationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()

        fusedLocationClient.requestLocationUpdates(
            currentLocationRequest,
            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    //todo: add this location to realtime database
                    if(p0.lastLocation == null) return
                    val markerOptions = MarkerOptions()
                    val latLng = LatLng(p0.lastLocation!!.latitude, p0.lastLocation!!.longitude)
                    markerOptions.position(latLng)
                    googleMap.clear()
                    googleMap.addMarker(markerOptions)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun registerActivityForLocationResult() {
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.e("requestPermissions", "isGranted: $isGranted")
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val isGranted =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        Log.e("isLocationPermissionGranted", "isGranted: $isGranted")
        return PackageManager.PERMISSION_GRANTED == isGranted
    }

    private fun shouldShowRationale(): Boolean {
        var shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldShowRationale
        ) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Location Permission")
                .setMessage("Location access is required to attach task to specific location")
                .setPositiveButton("ok") { dialog, _ ->
                    dialog.dismiss()
                    requestLocationPermission()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

            alertDialog.show()
        }
        return shouldShowRationale
    }

    fun requestLocationPermission() {

        locationPermissionRequest.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
}