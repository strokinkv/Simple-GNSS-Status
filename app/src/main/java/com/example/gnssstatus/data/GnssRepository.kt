package com.example.gnssstatus.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.gnssstatus.domain.GnssUiState
import com.example.gnssstatus.domain.SatelliteInfo
import com.example.gnssstatus.domain.SatelliteSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GnssRepository(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isStarted = false

    private val _state = MutableStateFlow(GnssUiState(isLocationEnabled = isLocationProviderEnabled()))
    val state: StateFlow<GnssUiState> = _state.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _state.value = _state.value.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                horizontalAccuracyMeters = location.takeIf { it.hasAccuracy() }?.accuracy,
                isLocationEnabled = isLocationProviderEnabled(),
                errorMessage = null,
            )
        }

        override fun onProviderEnabled(provider: String) {
            _state.value = _state.value.copy(isLocationEnabled = isLocationProviderEnabled(), errorMessage = null)
        }

        override fun onProviderDisabled(provider: String) {
            _state.value = _state.value.copy(isLocationEnabled = isLocationProviderEnabled())
        }

        @Deprecated("Deprecated in Android API")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satellites = buildList {
                for (index in 0 until status.satelliteCount) {
                    val satellite = runCatching {
                        SatelliteInfo(
                            svid = status.getSvid(index),
                            system = mapConstellationType(status.getConstellationType(index)),
                            cn0DbHz = status.getCn0DbHz(index),
                            usedInFix = status.usedInFix(index),
                        )
                    }.getOrNull()
                    if (satellite != null) add(satellite)
                }
            }

            _state.value = _state.value.copy(
                satellites = satellites.sortedWith(
                    compareByDescending<SatelliteInfo> { it.usedInFix }
                        .thenByDescending { it.cn0DbHz }
                ),
                isLocationEnabled = isLocationProviderEnabled(),
                errorMessage = null,
            )
        }
    }

    fun start() {
        if (isStarted) return

        if (!hasFineLocationPermission()) {
            _state.value = _state.value.copy(errorMessage = "Нет разрешения на точную геолокацию")
            return
        }

        if (!isLocationProviderEnabled()) {
            _state.value = _state.value.copy(isLocationEnabled = false)
            return
        }

        isStarted = startWithPermission()
    }

    fun stop() {
        if (!isStarted) return
        locationManager.removeUpdates(locationListener)
        locationManager.unregisterGnssStatusCallback(gnssCallback)
        isStarted = false
    }

    private fun startWithPermission(): Boolean {
        return try {
            if (isGpsProviderEnabled()) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1_000L,
                    0f,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
            locationManager.registerGnssStatusCallback(gnssCallback, mainHandler)
            true
        } catch (exception: SecurityException) {
            _state.value = _state.value.copy(errorMessage = "Нет доступа к точной геолокации")
            false
        }
    }

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private fun isGpsProviderEnabled(): Boolean =
        runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)

    private fun isLocationProviderEnabled(): Boolean =
        runCatching { locationManager.getProviders(true).isNotEmpty() }.getOrDefault(false)
}

fun mapConstellationType(type: Int): SatelliteSystem = when (type) {
    GnssStatus.CONSTELLATION_GPS -> SatelliteSystem.Gps
    GnssStatus.CONSTELLATION_GLONASS -> SatelliteSystem.Glonass
    GnssStatus.CONSTELLATION_GALILEO -> SatelliteSystem.Galileo
    GnssStatus.CONSTELLATION_BEIDOU -> SatelliteSystem.Beidou
    GnssStatus.CONSTELLATION_QZSS -> SatelliteSystem.Qzss
    GnssStatus.CONSTELLATION_SBAS -> SatelliteSystem.Sbas
    GnssStatus.CONSTELLATION_IRNSS -> SatelliteSystem.Irnss
    else -> SatelliteSystem.Unknown
}
