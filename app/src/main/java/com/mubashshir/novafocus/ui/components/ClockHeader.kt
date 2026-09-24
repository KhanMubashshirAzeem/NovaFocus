package com.mubashshir.novafocus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.TextPrimary
import com.mubashshir.novafocus.ui.theme.TextSecondary

@Composable
fun ClockHeader(
    time: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(
            start = LauncherDimensions.ScreenHorizontalPadding,
            top = LauncherDimensions.ScreenTopPadding,
            bottom = 28.dp
        )
    ) {
        Text(
            text = time.ifEmpty { "--:--" },
            color = TextPrimary,
            fontSize = LauncherDimensions.ClockFontSize,
            fontWeight = FontWeight.SemiBold,
            lineHeight = LauncherDimensions.ClockFontSize
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = date,
            color = TextSecondary,
            fontSize = LauncherDimensions.DateFontSize,
            fontWeight = FontWeight.Normal
        )
    }
}
