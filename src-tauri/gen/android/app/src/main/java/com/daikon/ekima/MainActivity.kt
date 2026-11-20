package com.daikon.ekima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.webkit.WebView
import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.tauri.TauriActivity

class MainActivity : TauriActivity() {
  private var notificationBridge: EkimaNotificationBridge? = null
  private var pendingNotification: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val decorView = window.decorView
        val controller = WindowInsetsControllerCompat(
            window,
            decorView
        )

        // ステータスバー：#f5f5f5
        val statusBarColor = Color.rgb(16, 24, 40)
        window.statusBarColor = statusBarColor
        controller.isAppearanceLightStatusBars = true

        // ナビゲーションバー：透明
        window.navigationBarColor = Color.TRANSPARENT
        controller.isAppearanceLightNavigationBars = true

        // ステータスバー分だけアプリを下にずらす
        ViewCompat.setOnApplyWindowInsetsListener(
            decorView
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                0,
                systemBars.top,
                0,
                0
            )

            // ステータスバー領域の背景色
            view.setBackgroundColor(statusBarColor)

            insets
        }

        ViewCompat.requestApplyInsets(decorView)
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

  fun reportLocationPermissionRequired() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        2002
      )
    }
  }

  override fun onDestroy() {
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
      notificationBridge?.updateNearestStation(pendingNotification ?: "情報取得中")
      pendingNotification = null
    }
  }
}
