package com.ycngmn.notubetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ycngmn.notubetv.ui.screens.YoutubeWV
import com.ycngmn.notubetv.ui.theme.NoTubeTVTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ❌ HAPUS hardcoded resolution (ini bahaya di API 23 & non-TV device)
        // window.setLayout(3840, 2160)

        // ❌ EdgeToEdge bisa bikin issue di WebView TV mode
        // enableEdgeToEdge()

        setContent {
            NoTubeTVTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    YoutubeWV()
                }
            }
        }
    }
}
