package com.mubashshir.novafocus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.TextMuted
import com.mubashshir.novafocus.ui.theme.TextPrimary

@Composable
fun FilteredAppsSection(
    letter: Char,
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = LauncherDimensions.ScreenTopPadding)
    ) {
        // Large letter header at top-left
        Text(
            text = letter.toString(),
            color = TextPrimary,
            fontSize = LauncherDimensions.HeaderLetterFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = LauncherDimensions.ScreenHorizontalPadding,
                bottom = 24.dp
            )
        )

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
