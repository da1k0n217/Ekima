package com.daikon.ekima

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.content.Intent

class EkimaNotificationBridge(private val activity: MainActivity) {
    companion object {
        private const val CHANNEL_ID = "ekima-nearest-station"
        private const val NOTIFICATION_ID = 1001
    }

    private val context: Context
        get() = activity

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun requestNotificationPermission() {
        mainHandler.post {
            if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                activity.requestNotificationPermission()
                return@post
            }
            updateNearestStationOnMainThread("情報取得中")
        }
    }

    @JavascriptInterface
    fun updateNearestStation(stationName: String) {
        mainHandler.post {
            updateNearestStationOnMainThread(stationName)
        }

        @JavascriptInterface
        fun startBackgroundTracking(stationsJson: String) {
            mainHandler.post {
                if (!canUseLocation()) {
                    activity.reportLocationPermissionRequired()
                    return@post
                }
                val intent = Intent(activity, EkimaLocationService::class.java)
                    .setAction(EkimaLocationService.ACTION_START)
                    .putExtra("stations", stationsJson)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent)
                } else {
                    activity.startService(intent)
                }
            }
        }

        @JavascriptInterface
        fun stopBackgroundTracking() {
            mainHandler.post {
                activity.stopService(
                    Intent(activity, EkimaLocationService::class.java)
                        .setAction(EkimaLocationService.ACTION_STOP)
                )
            }
        }
    }

    private fun updateNearestStationOnMainThread(stationName: String) {
        if (stationName.isBlank()) return
        if (!canPostNotifications()) {
            activity.setPendingNotification(stationName)
            activity.requestNotificationPermission()
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "最寄り駅",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "現在の最寄り駅を表示します"
            }
        )

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(stationName)
            .setContentText("Ekima")
            .setStyle(Notification.BigTextStyle().bigText("最寄り駅：$stationName"))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)

        if (Build.VERSION.SDK_INT >= 36) {
            builder.setRequestPromotedOngoing(true)
        }

        manager.notify(NOTIFICATION_ID, builder.build())
    }

    @JavascriptInterface
    fun clearNearestStation() {
        mainHandler.post {
            context.getSystemService(NotificationManager::class.java)
                ?.cancel(NOTIFICATION_ID)
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun canUseLocation(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
