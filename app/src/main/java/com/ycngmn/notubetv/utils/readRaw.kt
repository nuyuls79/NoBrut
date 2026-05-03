package com.ycngmn.notubetv.utils

import android.content.Context
import java.io.IOException

fun readRaw(context: Context, resId: Int): String {

    return try {

        context.resources.openRawResource(resId)
            .bufferedReader()
            .use { it.readText() }

    } catch (e: IOException) {
        "" // fallback aman
    } catch (e: Exception) {
        "" // fallback untuk semua error lain
    }
}
