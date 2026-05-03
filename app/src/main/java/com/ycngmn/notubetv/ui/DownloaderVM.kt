package com.ycngmn.notubetv.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UpdateViewModel : ViewModel() {

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun downloadAndInstall(
        context: Context,
        url: String,
        tagName: String,
        isShowDialog: MutableState<Boolean>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apkFile = downloadApk(context, url, tagName)

                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Download failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } finally {

                withContext(Dispatchers.Main) {
                    isShowDialog.value = false
                }
            }
        }
    }

    private suspend fun downloadApk(
        context: Context,
        url: String,
        tagName: String
    ): File {

        val client = HttpClient(OkHttp)

        return try {

            val file = File(context.cacheDir, "NoTubeTV_$tagName.apk")

            val response = client.get(url)
            val total = response.contentLength() ?: -1L

            var downloaded = 0L

            response.bodyAsChannel().toInputStream().use { input ->
                file.outputStream().use { output ->

                    val buffer = ByteArray(8192) // ✅ lebih optimal
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        if (total > 0) {
                            val progress = ((downloaded * 100) / total).toInt()
                            _downloadProgress.value = progress.coerceIn(0, 100)
                        }
                    }
                }
            }

            file

        } finally {
            // ✅ WAJIB: tutup client biar tidak leak
            client.close()
        }
    }

    private fun installApk(context: Context, apkFile: File) {

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                apkUri,
                "application/vnd.android.package-archive"
            )

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Cannot open installer",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
