package com.example.aplikasipresensi.ui.presensi

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.databinding.ActivityPresensiKeluarBinding
import com.example.aplikasipresensi.databinding.ActivityPresensiMasukBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PresensiKeluarActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPresensiKeluarBinding

    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiKeluarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val loc = LatLng(-7.812879519672033, 110.37669502422007)
        mMap.addMarker(MarkerOptions().position(loc).title("Lokasi Kantor"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc))
    }

}