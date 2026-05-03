package com.ycngmn.notubetv.ui.screens

import android.app.Activity
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
    val activity = context as? Activity ?: return

    val state = rememberWebViewState("https://www.youtube.com/tv")
    val navigator = rememberWebViewNavigator()

    val jsScript = youtubeVM.scriptData.value
    val updateData = youtubeVM.updateData.value

    val loadingState = state.loadingState
    val exitTrigger = remember { mutableStateOf(false) }

    val isWebReady = remember { mutableStateOf(false) }
    val isJsInjected = remember { mutableStateOf(false) }

    // 🔙 Back handler
    BackHandler {
        if (state.loadingState is LoadingState.Finished) {
            try {
                navigator.evaluateJavaScript(readRaw(context, R.raw.back_bridge))
            } catch (_: Exception) {}
        } else {
            exitTrigger.value = true
        }
    }

    // 📦 Load script + update
    LaunchedEffect(Unit) {
        val script = fetchScripts()
        if (script != null) {
            youtubeVM.setScript(script)
        }

        getUpdate(context, navigator) { update ->
            if (update != null) {
                youtubeVM.setUpdate(update)
            }
        }
    }

    // ⚡ SAFE JS injection (anti crash box)
    LaunchedEffect(loadingState, jsScript, isWebReady.value) {

        if (
            loadingState is LoadingState.Finished &&
            jsScript != null &&
            isWebReady.value &&
            !isJsInjected.value
        ) {

            isJsInjected.value = true

            kotlinx.coroutines.delay(1500)

            try {
                navigator.evaluateJavaScript(jsScript)
            } catch (_: Exception) {}
        }
    }

    // 📌 update dialog
    if (updateData != null) {
        UpdateDialog(updateData, navigator)
    }

    // ❌ exit app
    if (exitTrigger.value) {
        activity.finish()
    }

    // 🔄 splash loading
    val loading = state.loadingState as? LoadingState.Loading
    if (loading != null) {
        SplashLoading(loading.progress.coerceIn(0f, 1f))
    }

    // 🌐 WEBVIEW SAFE MODE
    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        platformWebViewParams = permHandler(context),
        captureBackPresses = false,
        onCreated = { webView ->

            isWebReady.value = true

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

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

                // 🔥 IMPORTANT: SOFTWARE RENDERING (anti crash Android box)
                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                setInitialScale(30)

                // ⚠️ DISABLED FOR STABILITY (enable later if stable)
                // addJavascriptInterface(ExitBridge(exitTrigger), "ExitBridge")
                // addJavascriptInterface(NetworkBridge(navigator), "NetworkBridge")
            }
        }
    )
}
