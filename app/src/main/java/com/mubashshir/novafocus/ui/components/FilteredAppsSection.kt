package com.mubashshir.novafocus.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.RippleOverlay
import com.mubashshir.novafocus.ui.theme.TextMuted
import com.mubashshir.novafocus.ui.theme.TextPrimary
import com.mubashshir.novafocus.ui.theme.TextSecondary

@Composable
fun FilteredAppsSection(
    letter: Char,
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to return to home
    BackHandler(onBack = onBackToHome)

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = LauncherDimensions.ScreenTopPadding)
            .pointerInput(Unit) {
                // Swipe right to return to Home
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 35f) {
                        onBackToHome()
                    }
                }
            }
    ) {
        // Letter header row with clickable return affordance
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, color = RippleOverlay),
                    onClick = onBackToHome
                )
                .padding(
                    start = LauncherDimensions.ScreenHorizontalPadding,
                    bottom = 20.dp
                )
        ) {
            Text(
                text = "‹",
                color = TextSecondary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = letter.toString(),
                color = TextPrimary,
                fontSize = LauncherDimensions.HeaderLetterFontSize,
                fontWeight = FontWeight.Bold
            )
        }

        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LauncherDimensions.ScreenHorizontalPadding, top = 32.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "No apps",
                    color = TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = apps,
                    key = { it.id }
                ) { app ->
                    AppRowItem(
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
