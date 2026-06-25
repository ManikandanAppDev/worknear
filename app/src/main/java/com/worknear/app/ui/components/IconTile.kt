package com.worknear.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

/**
 * A flat-blue glyph centered inside a soft, tinted rounded-square ("squircle") tile.
 * This is the shared icon treatment used across the WorkNear redesign.
 */
@Composable
fun IconTile(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    tileSize: Dp = 52.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 16.dp,
    tint: Color = PrimaryBlue,
    background: Color = PrimaryBlue.copy(alpha = 0.10f)
) {
    Box(
        modifier = modifier
            .size(tileSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** A compact, self-sizing primary pill button (does not force fillMaxWidth). */
@Composable
fun PrimaryPillButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) PrimaryBlue else PrimaryBlue.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = sansProText
        )
    }
}
