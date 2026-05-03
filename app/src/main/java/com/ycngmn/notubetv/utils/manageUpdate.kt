package com.ycngmn.notubetv.utils

import android.content.Context
import android.os.Build
import com.multiplatform.webview.web.WebViewNavigator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ReleaseData(
    val tagName: String,
    val changelog: String,
    val downloadUrl: String
)

// ✅ Fetch update (aman & tidak leak)
suspend fun fetchUpdate(): ReleaseData? = withContext(Dispatchers.IO) {

    val fetchUrl = "https://api.github.com/repos/ycngmn/notubetv/releases/latest"
    val client = HttpClient(OkHttp)

    try {
        val response: String = client.get(fetchUrl).body()
        val res = JSONObject(response)

        val commitSHA = Regex("\\b[a-fA-F0-9]{40}\\b")

        val assets = res.optJSONArray("assets")
        val downloadUrl = if (assets != null && assets.length() > 0) {
            assets.getJSONObject(0).optString("browser_download_url", "")
        } else ""

        if (downloadUrl.isEmpty()) return@withContext null

        ReleaseData(
            tagName = res.optString("tag_name", ""),
            changelog = res.optString("body", "")
                .substringAfter("</ins>", "")
                .replace(commitSHA, "")
                .replace(Regex("\\s{2,}"), " ")
                .trim(),
            downloadUrl = downloadUrl
        )

    } catch (e: Exception) {
        null
    } finally {
        client.close() // ✅ WAJIB
    }
}

// ✅ Compare version dengan benar (fix bug besar)
private fun isNewerVersion(remote: String, local: String): Boolean {
    return try {
        val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val l = local.split(".").map { it.toIntOrNull() ?: 0 }

        val maxSize = maxOf(r.size, l.size)

        for (i in 0 until maxSize) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }

            if (rv > lv) return true
            if (rv < lv) return false
        }

        false
    } catch (e: Exception) {
        false
    }
}

// ✅ Main checker
suspend fun getUpdate(
    context: Context,
    navigator: WebViewNavigator,
    callback: (ReleaseData?) -> Unit
) {

    try {
        val remoteRelease = fetchUpdate() ?: return callback(null)

        val remoteVersion = remoteRelease.tagName.removePrefix("v")
        val localVersion = getLocalVersion(context)

        if (isNewerVersion(remoteVersion, localVersion)) {

            getSkipVersion(navigator) { skip ->

                val skipVersion = skip
                    ?.removeSurrounding("\"")
                    ?.removePrefix("v")

                if (skipVersion != remoteVersion) {
                    callback(remoteRelease)
                } else {
                    callback(null)
                }
            }

        } else {
            callback(null)
        }

    } catch (e: Exception) {
        callback(null)
    }
}

// ✅ Aman untuk API 23+
private fun getLocalVersion(context: Context): String {
    return try {
        val pInfo = if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        pInfo.versionName ?: "0.0.0"

    } catch (e: Exception) {
        "0.0.0"
    }
}

// ✅ JS bridge safe callback
fun getSkipVersion(
    navigator: WebViewNavigator,
    callback: (String?) -> Unit
) {
    try {
        navigator.evaluateJavaScript("configRead('skipVersionName')") {
            callback(it)
        }
    } catch (e: Exception) {
        callback(null)
    }
}
