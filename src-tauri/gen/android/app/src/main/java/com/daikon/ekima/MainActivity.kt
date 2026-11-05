package com.daikon.ekima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge

class MainActivity : TauriActivity() {
  private var notificationBridge: EkimaNotificationBridge? = null
  private var pendingNotification: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
  }

  override fun onWebViewCreate(webView: WebView) {
    val bridge = EkimaNotificationBridge(this)
    notificationBridge = bridge
    webView.addJavascriptInterface(bridge, "EkimaNotifications")
  }

  fun requestNotificationPermission() {
    runOnUiThread {
      if (Build.VERSION.SDK_INT >= 33 &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
      } else {
        notificationBridge?.updateNearestStation(pendingNotification ?: "位置情報を取得中")
        pendingNotification = null
      }
    }
  }

  fun setPendingNotification(stationName: String) {
    pendingNotification = stationName
  }

  override fun onDestroy() {
    notificationBridge?.clearNearestStation()
    pendingNotification = null
    super.onDestroy()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 2001 &&
      grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
    ) {
      notificationBridge?.updateNearestStation(pendingNotification ?: "位置情報を取得中")
      pendingNotification = null
    }
  }
}
