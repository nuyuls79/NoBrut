package com.ycngmn.notubetv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ycngmn.notubetv.R

@Composable
fun SplashLoading(progress: Float) {

    // ✅ FIX: pastikan progress tidak lebih dari 1.0
    val animatedProgress by animateFloatAsState(
        targetValue = (progress * 1.5f).coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.banner_fg),
                contentDescription = null,
                modifier = Modifier.padding(bottom = 80.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(R.drawable.toys_fan_24px),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // ✅ FIX: gunakan progress langsung (bukan lambda)
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF0000),
                    trackColor = Color.LightGray,
                    strokeCap = StrokeCap.Square
                )
            }
        }
    }
}
