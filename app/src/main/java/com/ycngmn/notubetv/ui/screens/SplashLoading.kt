package com.ycngmn.notubetv.ui.screens

import android.app.Activity
import android.view.View
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
import com.ycngmn.notubetv.ui.YoutubeVM
import com.ycngmn.notubetv.utils.fetchScripts
import com.ycngmn.notubetv.utils.readRaw

@Composable
fun YoutubeWV(
    youtubeVM: YoutubeVM = viewModel(),
    skipSplash: Boolean = true
) {

    val context = LocalContext.current
    val activity = context as? Activity ?: return

    val state = rememberWebViewState("https://www.youtube.com/tv")
    val navigator = rememberWebViewNavigator()

    val jsScript = youtubeVM.scriptData
    val updateData = youtubeVM.updateData

    val loadingState = state.loadingState
    val exitTrigger = remember { mutableStateOf(false) }

    val isWebReady = remember { mutableStateOf(false) }
    val isJsInjected = remember { mutableStateOf(false) }

    val isBootSafe = remember { skipSplash }

    // 🔙 Back handler
    BackHandler {
        try {
            navigator.evaluateJavaScript(readRaw(context, com.ycngmn.notubetv.R.raw.back_bridge))
        } catch (_: Exception) {
            exitTrigger.value = true
        }
    }

    // 📦 Load script (safe)
    LaunchedEffect(Unit) {
        try {
            val script = fetchScripts()
            if (script != null) {
                youtubeVM.setScript(script)
            }
        } catch (_: Exception) {}

        // update tetap jalan tapi aman
        try {
            com.ycngmn.notubetv.utils.getUpdate(context, navigator) { update ->
                if (update != null) {
                    youtubeVM.setUpdate(update)
                }
            }
        } catch (_: Exception) {}
    }

    // ⚡ SAFE JS INJECTION (anti crash box)
    LaunchedEffect(loadingState, jsScript, isWebReady.value) {

        if (
            loadingState is LoadingState.Finished &&
            jsScript != null &&
            isWebReady.value &&
            !isJsInjected.value &&
            isBootSafe
        ) {

            isJsInjected.value = true

            kotlinx.coroutines.delay(2000)

            try {
                navigator.evaluateJavaScript(jsScript)
            } catch (_: Exception) {}
        }
    }

    // ❌ OPTIONAL SPLASH (DISABLED BY DEFAULT)
    if (!skipSplash) {
        val loading = state.loadingState as? LoadingState.Loading
        if (loading != null) {
            com.ycngmn.notubetv.ui.screens.SplashLoading(
                loading.progress.coerceIn(0f, 1f)
            )
        }
    }

    // 🚀 WEBVIEW CORE (SAFE MODE)
    WebView(
        modifier = Modifier.fillMaxSize(),
        state = state,
        navigator = navigator,
        platformWebViewParams = com.ycngmn.notubetv.utils.permHandler(context),
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

                // 🔥 IMPORTANT: SAFE MODE FOR ANDROID BOX
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

                setInitialScale(30)

                // ⚠️ DISABLED (biar tidak crash 15%)
                // addJavascriptInterface(ExitBridge(exitTrigger), "ExitBridge")
                // addJavascriptInterface(NetworkBridge(navigator), "NetworkBridge")
            }
        }
    )
}
