package com.mubashshir.novafocus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mubashshir.novafocus.data.model.AppItem
import com.mubashshir.novafocus.ui.theme.LauncherDimensions
import com.mubashshir.novafocus.ui.theme.RippleOverlay
import com.mubashshir.novafocus.ui.theme.SurfaceVariantDark
import com.mubashshir.novafocus.ui.theme.TextPrimary

@Composable
fun AppRowItem(
    app: AppItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = RippleOverlay),
                onClick = onClick
            )
            .padding(
                horizontal = LauncherDimensions.ScreenHorizontalPadding,
                vertical = LauncherDimensions.AppItemPaddingVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(LauncherDimensions.AppIconSize)
                    .clip(RoundedCornerShape(LauncherDimensions.AppIconCornerRadius))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(LauncherDimensions.AppIconSize)
                    .clip(RoundedCornerShape(LauncherDimensions.AppIconCornerRadius))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.firstChar.toString(),
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.width(LauncherDimensions.AppItemSpacing))

        Text(
            text = app.label,
            color = TextPrimary,
            fontSize = LauncherDimensions.AppItemTitleFontSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
