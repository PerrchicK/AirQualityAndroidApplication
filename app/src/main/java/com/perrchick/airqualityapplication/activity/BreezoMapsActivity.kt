package com.perrchick.airqualityapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.perrchick.airqualityapplication.AirQualityApplication
import com.perrchick.airqualityapplication.R
import com.perrchick.airqualityapplication.util.Configurations
import com.perrchick.airqualityapplication.util.PrivateEventBus
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class BreezoMapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
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
        //super.onCreate(savedInstanceState)

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

        updateNewBaqiLevel(AirQualityApplication.instance()?.currentBaqi)

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
//                AppLogger.log(intent)
                //intent.extras.getInt("baqi")
                AirQualityApplication.instance()?.currentBaqi?.let {
                    updateNewBaqiLevel(it)
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
            txtBaqi.text = "~~"//getString(R.string.baqi_map_title_digit_format, baqi)
        }

        baqi?.let {
            txtBaqi.text = "$it"//getString(R.string.baqi_map_title_digit_format, baqi)
        }
    }

    private fun addBaqiMarker(currentLocation: LatLng? = null, animated: Boolean = false) {
        googleMap?.let { _ ->
            if (currentLocation == null) {
                AirQualityApplication.instance()?.currentLocation?.let { currentLocation ->
                    setLocation(currentLocation, DEFAULT_ZOOM_LEVEL, animated = animated)
                }
            } else {
                setLocation(currentLocation, animated = animated)
            }
        }
    }

    private fun setLocation(currentLocation: LatLng, zoom: Float? = null, animated: Boolean = false) {
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

            var markerOptions = MarkerOptions()
                    .position(currentLocation)
                    .flat(true)
                    .visible(true)
                    //.title(Strings.localized(R.string.you_are_here))
                    .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_compass))

            AirQualityApplication.instance()?.currentBaqi?.let { baqi ->
                markerOptions = markerOptions.title("$baqi")
            }

            baqiMarker?.remove()
            baqiMarker = googleMap?.addMarker(markerOptions)
        }
    }

    override fun onMapClick(latLng: LatLng?) {
        latLng?.let {
            updateNewBaqiLevel(null)
            AirQualityApplication.instance()?.currentLocation = it
            addBaqiMarker(latLng, animated = true)
        } // ... be :)
    }
}
