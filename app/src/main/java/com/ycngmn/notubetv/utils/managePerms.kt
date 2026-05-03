package com.ycngmn.notubetv.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.multiplatform.webview.web.AccompanistWebChromeClient
import com.multiplatform.webview.web.PlatformWebViewParams

@Composable
fun permHandler(context: Context): PlatformWebViewParams {

    // ✅ simpan request sementara
    var pendingRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->

        val request = pendingRequest
        pendingRequest = null

        if (granted && request != null) {
            request.grant(request.resources)
        } else {
            request?.deny()
        }
    }

    val chrome = remember {
        object : AccompanistWebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest) {

                // ✅ hanya handle audio
                if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in request.resources) {

                    if (hasPermission(context)) {
                        request.grant(request.resources)
                    } else {
                        pendingRequest = request
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                } else {
                    // ✅ deny resource lain (lebih aman)
                    request.deny()
                }
            }
        }
    }

    return PlatformWebViewParams(chromeClient = chrome)
}

// ✅ helper permission check
fun hasPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}
