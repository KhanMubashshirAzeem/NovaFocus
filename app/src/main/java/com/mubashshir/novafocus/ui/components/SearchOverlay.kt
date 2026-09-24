package com.mubashshir.novafocus.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.ui.theme.BackgroundDark
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.SurfaceVariantDark
import com.mubashshir.novafocus.ui.theme.TextMuted
import com.mubashshir.novafocus.ui.theme.TextPrimary

@Composable
fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler {
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
        // Search Input Bar
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search apps",
                    color = TextMuted,
                    fontSize = 17.sp
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    results.firstOrNull()?.let(onAppClick)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LauncherDimensions.ScreenHorizontalPadding)
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (results.isEmpty() && query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LauncherDimensions.ScreenHorizontalPadding, top = 32.dp)
            ) {
                Text(
                    text = "No apps found",
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = results,
                    key = { it.id }
                ) { app ->
                    AppRowItem(
                        app = app,
                        onClick = {
                            keyboardController?.hide()
                            onAppClick(app)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
