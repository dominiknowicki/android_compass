package pl.donow.compass

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var currentOrientation = 0f
    private var orientationMatrix = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private val sensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this) as FusedLocationProviderClient
    }
    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onLocationChanged(locationResult?.lastLocation)
            }
        }
    }
    private val requestCode = 34

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (!checkIfPermissionsGranted())
            showRequestPermissionRationale()
        else
            requestLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        if (requestCode == this.requestCode) {
            when {
                grantResults.isEmpty() -> {
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> requestLocationUpdates()
                else -> {
                    coordinatesContainerView.visibility = View.GONE
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Implements SensorEventListener.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Implements SensorEventListener.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = SensorManager.getOrientation(rotationMatrix, orientationMatrix)[0]
            rotateCompassView(Math.toDegrees(orientation.toDouble()).toFloat())
        }
    }

    /**
     * Rotates view that represents the compass.
     */
    private fun rotateCompassView(orientation: Float) {
        val animation = RotateAnimation(
            currentOrientation,
            -orientation,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.duration = 210
        animation.fillAfter = true
        compassImageView.startAnimation(animation)
        currentOrientation = -orientation
    }

    /**
     * Called when receives GPS position.
     */
    private fun onLocationChanged(location: Location?) {
        if (coordinatesContainerView.visibility != View.VISIBLE)
            coordinatesContainerView.visibility = View.VISIBLE
        location?.let {
            latitudeTextView.text = it.latitude.latitudeToDms()
            longitudeTextView.text = it.longitude.longitudeToDms()
        }
    }

    /**
     * Request for periodic location updates.
     */
    private fun requestLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(2000)
                    .setFastestInterval(1000),
                locationCallback,
                Looper.myLooper()
            )
        } catch (ex: SecurityException) {
            showRequestPermissionRationale()
        }
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private fun checkIfPermissionsGranted(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Show request for permissions with rationale if possible.
     */
    private fun showRequestPermissionRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(findViewById(R.id.activity_main), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok) { requestPermission() }
                .show()
        } else
            requestPermission()
    }

    /**
     * Starts request for permissions.
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            requestCode
        )
    }
}
