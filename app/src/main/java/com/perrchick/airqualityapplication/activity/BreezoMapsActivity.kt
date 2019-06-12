package com.perrchick.airqualityapplication.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.R
import com.perrchick.airqualityapplication.bl.DecisionMaker
import com.perrchick.airqualityapplication.util.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class BreezoMapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE: Int = 0x1001
    }

    private var baqiObserver: PrivateEventBus.Receiver? = null
    private var locationObserver: PrivateEventBus.Receiver? = null

    private val DEFAULT_ZOOM_LEVEL: Float = 15.0F
    private val MINIMUM_ZOOM_LEVEL: Float = 6.0F
    private val MAXIMUM_ZOOM_LEVEL: Float = 15.0F

    private lateinit var txtBaqi: TextView
    private var googleMap: GoogleMap? = null

    private var tileOverlay: TileOverlay? = null
    private var baqiMarker: Marker? = null

    private var breezoTileProvider: TileProvider? = BreezoTileProvider()

    class BreezoTileProvider : UrlTileProvider(256, 256) {
        private val TILE_SERVER_URL = "https://tiles.breezometer.com/%d/%d/%d.png?key=%s"

        override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
            /* Define the URL pattern for the tile images */
            val s = String.format(Locale.US, TILE_SERVER_URL, zoom, x, y, Configurations.ApiKey())
            if (!checkTileExists(x, y, zoom)) {
                return null
            }

            try {
                return URL(s)
            } catch (e: MalformedURLException) {
                throw AssertionError(e)
            }

        }

        /*
	    * Check that the tile server supports the requested x, y and zoom.
	    * Complete this stub according to the tile range you support.
	    * If you support a limited range of tiles at different zoom levels, then you
	    * need to define the supported x, y range at each zoom level.
	    */
        private fun checkTileExists(x: Int, y: Int, zoom: Int): Boolean {
            val minZoom = 0
            val maxZoom = 16

            return !(zoom < minZoom || zoom > maxZoom)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            super.onCreate(it)
        } ?: run {
            // Prevents a silly crash: https://stackoverflow.com/questions/44597348/illegalargumentexception-savedinstancestate-specified-as-non-null-is-null
            super.onCreate(Bundle())
        }

        setContentView(R.layout.activity_breezo_maps)

        txtBaqi = findViewById(R.id.txt_baqi_level)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()

        updateNewBaqiLevel(null)

        locationObserver = PrivateEventBus.createNewReceiver(object : PrivateEventBus.BroadcastReceiverListener {
            override fun onBroadcastReceived(intent: Intent, receiver: PrivateEventBus.Receiver) {
                intent.extras?.let { extras ->
                    if (extras.size() == 0) return
                    if (extras["location"] == null) return

                    val location: LatLng = extras["location"] as LatLng

                    setLocation(location, animated = true)
                }

                //@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                //if (intent.extras == null) return
            }
        }, PrivateEventBus.Action.UPDATE_LOCATION)

        baqiObserver = PrivateEventBus.createNewReceiver(object : PrivateEventBus.BroadcastReceiverListener {
            override fun onBroadcastReceived(intent: Intent, receiver: PrivateEventBus.Receiver) {
                intent.extras?.let {extras ->
                    (extras[Constants.Keys.Baqi] as? Int)?.let {
                        updateNewBaqiLevel(it)
                    } // ... be :)
                }
            }
        }, PrivateEventBus.Action.NEW_BAQI_ARRIVED)

        addBaqiMarker()
    }

    override fun onPause() {
        super.onPause()

        baqiObserver?.quit()
        locationObserver?.quit()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false

        googleMap.setMaxZoomPreference(MAXIMUM_ZOOM_LEVEL)
        googleMap.setMinZoomPreference(MINIMUM_ZOOM_LEVEL)

        googleMap.setOnMapClickListener(this)
        googleMap.setOnMarkerClickListener {
            true // prevents showing "navigate" icon on the map
        }

        tileOverlay = googleMap.addTileOverlay(TileOverlayOptions()
                .tileProvider(breezoTileProvider)
                .transparency(0.425f))

        addBaqiMarker()
    }

    private fun updateNewBaqiLevel(baqi: Int?) {
        if (baqi == null) {
            txtBaqi.text = "??"
        }

        baqi?.let {
            txtBaqi.text = "  BAQI: $it  "//getString(R.string.baqi_map_title_digit_format, baqi)
        }
    }

    private fun addBaqiMarker(currentLocation: LatLng? = null, animated: Boolean = false, value: Double? = null) {
        googleMap?.let { _ ->
            if (currentLocation != null) {
                setLocation(currentLocation, animated = animated, value = value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                googleMap?.isMyLocationEnabled = true
            }
        }
    }

    private fun setLocation(currentLocation: LatLng, zoom: Float? = null, animated: Boolean = false, value: Double? = null) {
        var _zoom: Float? = zoom
        if (_zoom == null && googleMap?.cameraPosition?.zoom != null) {
            _zoom = googleMap?.cameraPosition!!.zoom
        }

        _zoom?.let { z ->
            if (animated) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, z))
            } else {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, z))
            }

            //baqiMarker?.remove()
            value?.let {
                var markerOptions = MarkerOptions()
                    .position(currentLocation)
                    .flat(true)
                    .visible(true)
                    //.title(Strings.localized(R.string.you_are_here))
                    //.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_compass))
                        //TODO: Set color according to concentration value
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                    markerOptions = markerOptions.title("$value")

                baqiMarker = googleMap?.addMarker(markerOptions)
            }
        }
    }

    override fun onMapClick(latLng: LatLng?) {
        latLng?.let { currentLocation ->
            val pollutantName = "pm25"
            DecisionMaker.fetchPollutantValue(pollutantName, currentLocation.latitude,currentLocation.longitude) { value ->
                AppLogger.log(value)
                value?.let {
                    Utils.debugToast("'$pollutantName' concentration value: $it")
                    AirQualityApplication.shared()?.runOnUiThread {
                        addBaqiMarker(latLng, animated = true, value = value)
                    }
                }
            }
            updateNewBaqiLevel(null)
        }
    }
}
