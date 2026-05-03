package com.ycngmn.notubetv.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.delay

const val SCRIPTS_URL =
    "https://raw.githubusercontent.com/ycngmn/NoTubeTV/refs/heads/main/assets/userscripts.js"

// ✅ Fetch script dengan retry aman
suspend fun fetchScripts(): String? {

    val client = HttpClient(OkHttp)

    return try {

        repeat(5) { attempt -> // ✅ max 5x retry

            try {
                val response: String = client.get(SCRIPTS_URL).body()
                if (response.isNotBlank()) return response

            } catch (_: Exception) {
                // retry
            }

            // ✅ delay supaya tidak freeze CPU
            delay(1000L * (attempt + 1))
        }

        null // gagal ambil script

    } finally {
        // ✅ WAJIB: hindari memory leak
        client.close()
    }
}
