package com.example.aplikasipresensi.ui.presensi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aplikasipresensi.R
import com.example.aplikasipresensi.data.api.ApiConfig
import com.example.aplikasipresensi.data.api.ApiService
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
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.StringBuilder
import java.util.Locale
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private const val REQUEST_CODE_BACKGROUND = 67
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 68
private const val REQUEST_LOCATION_PERMISSION = 69
class PresensiMasukActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPresensiMasukBinding
    private var marker: Marker? = null
    private val CAMERA = 1
    private val CAMERA_REQUEST_CODE = 5
    lateinit var context: Context
    lateinit var wrapCamera : FrameLayout
    lateinit var cardSuccess: CardView
    lateinit var tvTime : TextView
    lateinit var tvDate : TextView
    lateinit var imgvCamera : ImageView
    lateinit var imgvUser : ImageView
    lateinit var btnBack: Button
    lateinit var loading : ProgressDialog
    private var mPhotoUri: Uri? = null
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this@PresensiMasukActivity

        token = intent.getStringExtra("token").toString()

        wrapCamera = findViewById(R.id.wrap_camera)
        imgvCamera = findViewById(R.id.imgv_camera)
        imgvUser = findViewById(R.id.imgv_user)
        cardSuccess = findViewById(R.id.card_success)
        tvTime = findViewById(R.id.tv_time)
        tvDate = findViewById(R.id.tv_date)
        btnBack = findViewById(R.id.btn_back)

        wrapCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }

        btnBack.setOnClickListener {
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            saveData()
        }

        isPermissionGranted()

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
                Locale.getDefault(), "-7.812879519672033, 110.37669502422007", latLng.latitude, latLng.longitude
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

    private fun isPermissionGrantedd(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGrantedd()) {
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

    private fun showOkSnackBar() {
        Snackbar.make(binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG)
            .setAction(android.R.string.ok) {
                checkDeviceLocationSettings()
            }.show()
    }

    private fun saveData() {
        loading = ProgressDialog.show(context, null, "Harap Tunggu...", true, false)
        var image: MultipartBody.Part? = null

        if (mPhotoUri != null) {
            val filePath = getRealPathFromURIPath(mPhotoUri!!, this)
            val file = File(filePath)

            val mFile = RequestBody.create(
                "image/*".toMediaTypeOrNull(), file
            )
            image = MultipartBody.Part.createFormData("image", file.name, mFile)
        }

        val apiConfig = ApiConfig()
        val mApiService: ApiService = apiConfig.getApiService()
        mApiService.uploadImage("Bearer ${token}", image)?.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(response.body().toString())
                        val nama_file = json.getString("nama_file")

                        if (nama_file != "") {
                            absen(nama_file)
                        }
                        else {
                            loading.dismiss()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    loading.dismiss()
                    Toast.makeText(context, "Gagal Upload Data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                loading.dismiss()
                Log.d("onFailure : ", t.message!!)
                Toast.makeText(context, "Koneksi Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun absen(foto: String) {
        val apiConfig = ApiConfig()
        val mApiService: ApiService = apiConfig.getApiService()
        val koordinate = "Lat: %1$.5f, Long: %2$.5f"

        mApiService.absen("Bearer ${token}", "1", foto, koordinate, binding.etKeterangan.text.toString())?.enqueue(object :
            Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(response.body().toString())
                        val status = json.getBoolean("status")

                        if (status) {
                            val dataObject = json.getJSONObject("data")
                            val created_at = dataObject.getString("created_at")

                            formatdateTime(created_at)

                            cardSuccess.visibility = View.VISIBLE
                            binding.btnSubmit.visibility = View.GONE
                            binding.etKeterangan.setText("")
                        }
                        else {
                            val message = json.getString("message")
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            loading.dismiss()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    loading.dismiss()
                }

                else {
                    loading.dismiss()
                    Toast.makeText(context, "Gagal Upload Data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                loading.dismiss()
                Log.d("onfailure : ", t.message!!)
                Toast.makeText(context, "Koneksi error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun formatdateTime(inputdate: String) {
        val dateTime = LocalDateTime.parse(inputdate, DateTimeFormatter.ISO_DATE_TIME)

        val formattedTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        val formattedDate =
            "${dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                    "${dateTime.dayOfMonth} " +
                    "${dateTime.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} " +
                    "${dateTime.year}"

        tvTime.text = formattedTime
        tvDate.text = formattedDate
    }

    fun isPermissionGranted() {
        if ((ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                        != PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(
                            context!!,
                            android.Manifest.permission.CAMERA
                        )!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ), CAMERA
            )
        }
    }

    fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val imageName = randomAlphaNumeric(20)
        val path = MediaStore.Images.Media.insertImage(
            inContext!!.contentResolver,
            inImage,
            imageName,
            null
        )
        return Uri.parse(path)
    }

    private fun getRealPathFromURIPath(contentURI: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
        return if (cursor == null) {
            contentURI.path
        }
        else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

    fun randomAlphaNumeric(count: Int): String {
        var count = count
        val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvyxyz0123456789"
        val builder = StringBuilder()

        while (count-- != 0) {
            val character = (Math.random() * ALPHA_NUMERIC_STRING.length).toInt()
            builder.append(ALPHA_NUMERIC_STRING[character])
        }
        return builder.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgvUser.visibility = View.VISIBLE
            imgvCamera.visibility = View.GONE
            imgvUser.setImageBitmap(imageBitmap)
            mPhotoUri = getImageUri(context, imageBitmap)
        }
    }
}