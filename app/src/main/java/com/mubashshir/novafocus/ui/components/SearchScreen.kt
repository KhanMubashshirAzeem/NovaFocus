package com.mubashshir.novafocus.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.ui.theme.BackgroundDark
import com.mubashshir.novafocus.ui.theme.DividerColor
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.RippleOverlay
import com.mubashshir.novafocus.ui.theme.SurfaceVariantDark
import com.mubashshir.novafocus.ui.theme.TextMuted
import com.mubashshir.novafocus.ui.theme.TextPrimary
import com.mubashshir.novafocus.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AppItem>,
    recentSearches: List<String>,
    allApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onClearRecents: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController =
        LocalSoftwareKeyboardController.current
    val interactionSource =
        remember { MutableInteractionSource() }

    BackHandler {
        keyboardController?.hide()
        onDismiss()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = LauncherDimensions.ScreenTopPadding)
    ) {
        // Top Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(
                            bounded = false,
                            radius = 22.dp,
                            color = RippleOverlay
                        ),
                        onClick = {
                            keyboardController?.hide()
                            onDismiss()
                        }), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Search Text Field
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search apps",
                        color = TextMuted,
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = "✕",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clickable {
                                    onQueryChange("")
                                }
                                .padding(8.dp))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        results.firstOrNull()?.let(onAppClick)
                    }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main List Content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (query.isEmpty()) {
                // Recent Searches Section (Up to 3 recent items)
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = LauncherDimensions.ScreenHorizontalPadding,
                                    vertical = 8.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT SEARCHES",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Clear",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.clickable { onClearRecents() })
                        }
                    }

                    items(recentSearches.take(3)) { recentTerm ->
                        val matchingApp = allApps.firstOrNull {
                            it.label.equals(
                                recentTerm, ignoreCase = true
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (matchingApp != null) {
                                        keyboardController?.hide()
                                        onAppClick(matchingApp)
                                    } else {
                                        onQueryChange(recentTerm)
                                    }
                                }
                                .padding(
                                    horizontal = LauncherDimensions.ScreenHorizontalPadding,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🕒",
                                fontSize = 16.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Text(
                                text = recentTerm,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "↗",
                                color = TextMuted,
                                fontSize = 18.sp
                            )
                        }
                    }

                    item {
                        HorizontalDivider(
                            color = DividerColor,
                            modifier = Modifier.padding(
                                horizontal = LauncherDimensions.ScreenHorizontalPadding,
                                vertical = 12.dp
                            )
                        )
                    }
                }

                // All Apps Header
                item {
                    Text(
                        text = "ALL APPS",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(
                            horizontal = LauncherDimensions.ScreenHorizontalPadding,
                            vertical = 8.dp
                        )
                    )
                }

                items(
                    items = results.ifEmpty { allApps },
                    key = { it.id }) { app ->
                    AppRowItem(
                        app = app, onClick = {
                            keyboardController?.hide()
                            onAppClick(app)
                        })
                }
            } else {
                // Active Search Results
                if (results.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = LauncherDimensions.ScreenHorizontalPadding,
                                    top = 40.dp
                                )
                        ) {
                            Text(
                                text = "No apps found for \"$query\"",
                                color = TextMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(
                        items = results,
                        key = { it.id }) { app ->
                        AppRowItem(
                            app = app, onClick = {
                                keyboardController?.hide()
                                onAppClick(app)
                            })
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
