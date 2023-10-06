package com.example.aplikasipresensi.ui.presensi

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.databinding.ActivityPresensiMasukBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

private const val REQUEST_CODE_BACKGROUND = 67
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 68
private const val REQUEST_LOCATION_PERMISSION = 69
class PresensiMasukActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPresensiMasukBinding
    private var marker: Marker? = null

    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setPoiClick(mMap)
        setMapLongCLick(mMap)

        val loc = LatLng(-7.812879519672033, 110.37669502422007)
        mMap.addMarker(MarkerOptions().position(loc).title("Lokasi Kantor"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc))

        enableMyLocation()
    }

    private fun setMapLongCLick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude
            )
            marker = map.addMarker(
                MarkerOptions().position(latLng).title("Dropped Pin").snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.isInfoWindowShown
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
            poiMarker?.showInfoWindow()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap.isMyLocationEnabled = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkDeviceLocationSettings()
            }
            else {
                requestQPermission()
            }
        }
        else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION
            )
        }
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestQPermission() {
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundPermission) {
            val hasBackgroundPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasBackgroundPermission) {
                checkDeviceLocationSettings()
            }
            else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ), REQUEST_CODE_BACKGROUND
                )
            }
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this, REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                }
                catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(ContentValues.TAG, "Error getting location settings resolution: "+ sendEx.message)
                }
            }
            else {
                showOkSnackBar()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mMap.isMyLocationEnabled = true
                checkDeviceLocationSettings()
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            checkDeviceLocationSettings()
        }
    }

    private fun showOkSettingsSnackBar() {
        Snackbar.make(binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG)
            .setAction("Tutup") {}.show()
    }

    private fun showOkSnackBar() {
        Snackbar.make(binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG)
            .setAction(android.R.string.ok) {
                checkDeviceLocationSettings()
            }.show()
    }
}