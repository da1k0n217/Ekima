package com.daikon.ekima

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import org.json.JSONArray
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class EkimaLocationService : Service(), LocationListener {
    companion object {
        const val ACTION_START = "com.daikon.ekima.START_LOCATION"
        const val ACTION_STOP = "com.daikon.ekima.STOP_LOCATION"
        private const val CHANNEL_ID = "ekima-location-service"
        private const val NOTIFICATION_ID = 1001
        private const val STATIONS_EXTRA = "stations"
    }

    private data class Station(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    private lateinit var locationManager: LocationManager
    private var stations: List<Station> = emptyList()

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LocationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        stations = parseStations(intent?.getStringExtra(STATIONS_EXTRA).orEmpty())
        startForeground(NOTIFICATION_ID, buildNotification("位置情報を取得中"))
        requestLocationUpdates()
        return START_STICKY
    }

    private fun requestLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        providers.filter { locationManager.isProviderEnabled(it) }.forEach { provider ->
            locationManager.requestLocationUpdates(provider, 5_000L, 10f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        val nearest = stations.minByOrNull {
            distanceMeters(location.latitude, location.longitude, it.latitude, it.longitude)
        }
        updateNotification(nearest?.name ?: "位置情報を取得中")
    }

    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit

    override fun onDestroy() {
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun parseStations(value: String): List<Station> {
        if (value.isBlank()) return emptyList()
        val json = JSONArray(value)
        return buildList {
            for (index in 0 until json.length()) {
                val station = json.getJSONObject(index)
                add(
                    Station(
                        station.getString("name"),
                        station.getDouble("latitude"),
                        station.getDouble("longitude")
                    )
                )
            }
        }
    }

    private fun updateNotification(stationName: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(stationName))
    }

    private fun buildNotification(stationName: String): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(stationName)
            .setContentText("Ekima")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "最寄り駅",
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    private fun distanceMeters(
        latitude1: Double,
        longitude1: Double,
        latitude2: Double,
        longitude2: Double
    ): Double {
        val radians = Math.PI / 180
        val deltaLatitude = (latitude2 - latitude1) * radians
        val deltaLongitude = (longitude2 - longitude1) * radians
        val latitude1Radians = latitude1 * radians
        val latitude2Radians = latitude2 * radians
        val a = sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
            cos(latitude1Radians) * cos(latitude2Radians) *
            sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
        return 2 * 6_371_000 * atan2(sqrt(a), sqrt(max(0.0, 1 - a)))
    }
}
