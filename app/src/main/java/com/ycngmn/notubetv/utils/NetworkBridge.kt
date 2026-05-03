package com.ycngmn.notubetv.utils

import android.webkit.JavascriptInterface
import com.multiplatform.webview.web.WebViewNavigator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.*

import org.json.JSONArray
import org.json.JSONObject

class NetworkBridge(
    private val navigator: WebViewNavigator
) {

    // ✅ single scope (lebih aman dari CoroutineScope() liar)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ✅ reuse client (lebih efisien)
    private val client = HttpClient(OkHttp)

    @JavascriptInterface
    fun fetch(url: String, videoId: String) {

        scope.launch {
            try {

                val body: String = client.get(url).body()

                val result =
                    if (body.trim().startsWith("[")) {
                        filterSponsorBlock(body, videoId)
                    } else {
                        body
                    }

                val safeJson = result
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")

                val js = "window.onNetworkBridgeResponse('$safeJson');"

                withContext(Dispatchers.Main) {
                    navigator.evaluateJavaScript(js)
                }

            } catch (e: Exception) {

                // optional: kirim error ke JS biar debug gampang
                val jsError = "window.onNetworkBridgeResponse('');"

                withContext(Dispatchers.Main) {
                    navigator.evaluateJavaScript(jsError)
                }
            }
        }
    }

    private fun filterSponsorBlock(body: String, videoId: String): String {
        return try {

            val json = JSONArray(body)

            for (i in 0 until json.length()) {
                val item: JSONObject = json.optJSONObject(i) ?: continue

                if (item.optString("videoID") == videoId) {
                    return item.toString()
                }
            }

            ""

        } catch (e: Exception) {
            ""
        }
    }

    // ⚠️ optional cleanup kalau nanti kamu dispose WebView
    fun destroy() {
        scope.cancel()
        client.close()
    }
}
