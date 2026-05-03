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
    val activity = context as? Activity ?: return

    val state = rememberWebViewState("https://www.youtube.com/tv")
    val navigator = rememberWebViewNavigator()

    // ✅ FIX: ambil VALUE dari State
    val jsScript = youtubeVM.scriptData.value
    val updateData = youtubeVM.updateData.value

    val loadingState = state.loadingState
    val exitTrigger = remember { mutableStateOf(false) }

    BackHandler {
        if (state.loadingState is LoadingState.Finished) {
            navigator.evaluateJavaScript(readRaw(context, R.raw.back_bridge))
        } else {
            exitTrigger.value = true
        }
    }

    // ✅ Fetch sekali saja (FIX nullable fetchScripts)
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

    // ✅ Inject JS aman
    LaunchedEffect(loadingState, jsScript) {
        if (loadingState == LoadingState.Finished && jsScript != null) {
            navigator.evaluateJavaScript(jsScript)
        }
    }

    if (updateData != null) {
        UpdateDialog(updateData, navigator)
    }

    if (exitTrigger.value) {
        activity.finish()
    }

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

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true)
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

                addJavascriptInterface(ExitBridge(exitTrigger), "ExitBridge")
                addJavascriptInterface(NetworkBridge(navigator), "NetworkBridge")

                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                setInitialScale(30)

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        }
    )
}
