package com.ycngmn.notubetv.ui.screens

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.ycngmn.notubetv.R
import com.ycngmn.notubetv.ui.YoutubeVM
import com.ycngmn.notubetv.ui.components.UpdateDialog
import com.ycngmn.notubetv.utils.*

@Composable
fun YoutubeWV(youtubeVM: YoutubeVM = viewModel()) {

    val context = LocalContext.current
    val activity = context as? Activity ?: return // ✅ FIX: safe cast

    val state = rememberWebViewState("https://www.youtube.com/tv")
    val navigator = rememberWebViewNavigator()

    val jsScript = youtubeVM.scriptData
    val updateData = youtubeVM.updateData

    val loadingState = state.loadingState
    val exitTrigger = remember { mutableStateOf(false) }

    // ✅ Handle back press safely
    BackHandler {
        if (state.loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(readRaw(context, R.raw.back_bridge))
        } else {
            exitTrigger.value = true
        }
    }

    // ✅ Fetch data once
    LaunchedEffect(Unit) {
        youtubeVM.setScript(fetchScripts())

        getUpdate(context, navigator) { update ->
            if (update != null) youtubeVM.setUpdate(update)
        }
    }

    // ✅ Inject JS ONLY once when finished
    LaunchedEffect(loadingState, jsScript) {
        if (loadingState == LoadingState.Finished && jsScript != null) {
            navigator.evaluateJavaScript(jsScript)
        }
    }

    // ✅ Show update dialog
    if (updateData != null) {
        UpdateDialog(updateData, navigator)
    }

    // ✅ Exit app
    if (exitTrigger.value) {
        activity.finish()
    }

    // ✅ Splash loading (safe progress)
    val loading = state.loadingState as? LoadingState.Loading
    if (loading != null) {
        SplashLoading(loading.progress.coerceIn(0f, 1f))
    }

    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        platformWebViewParams = permHandler(context),
        captureBackPresses = false,
        onCreated = { webView ->

            activity.window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

            // ✅ Cookie FIX for API 23
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            }

            state.webSettings.apply {

                customUserAgentString =
                    "Mozilla/5.0 Cobalt/25 (Sony, PS4, Wired)"

                isJavaScriptEnabled = true

                androidWebSettings.apply {
                    useWideViewPort = true
                    domStorageEnabled = true
                    hideDefaultVideoPoster = true
                    mediaPlaybackRequiresUserGesture = false
                }
            }

            webView.apply {

                // ✅ JS Bridge (safe)
                addJavascriptInterface(ExitBridge(exitTrigger), "ExitBridge")
                addJavascriptInterface(NetworkBridge(navigator), "NetworkBridge")

                // ✅ Hardware acceleration fix
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                // ⚠️ FIX: jangan terlalu kecil di device lama
                setInitialScale(30) // sebelumnya 25 → bisa bikin crash di beberapa device

                // ✅ Disable scrollbar
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        }
    )
}
