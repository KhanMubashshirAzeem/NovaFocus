package com.mubashshir.novafocus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mubashshir.novafocus.data.model.AppItem

@Composable
fun FavoritesSection(
    time: String,
    date: String,
    favorites: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ClockHeader(time = time, date = date)

        favorites.forEach { app ->
            AppRowItem(
                app = app,
                onClick = { onAppClick(app) }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
