package com.worknear.app.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PurpleGrey40
import com.worknear.app.ui.theme.sansProText

enum class WorkNearButtonType {
    FILLED,
    OUTLINED,

    /** Outlined button styled in red for destructive actions (e.g. Cancel booking). */
    DANGER
}

@Composable
fun WorkNearButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: WorkNearButtonType = WorkNearButtonType.FILLED,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fontFamily: FontFamily = sansProText,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(16.dp)

    when (buttonType) {

        WorkNearButtonType.FILLED -> {

            Button(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.4f)
                )
            ) {

                ButtonContent(
                    text = text,
                    loading = loading,
                    icon = icon,
                    textColor = Color.White
                )
            }
        }

        WorkNearButtonType.OUTLINED -> {

            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = shape,
                border = BorderStroke(
                    width = 1.dp,
                    color = PurpleGrey40.copy(alpha = 0.45f)
                )
            ) {

                ButtonContent(
                    text = text,
                    loading = loading,
                    icon = icon,
                    textColor = DarkText.copy(alpha = 0.75f)
                )
            }
        }

        WorkNearButtonType.DANGER -> {

            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = shape,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = ErrorRed.copy(alpha = 0.7f)
                )
            ) {

                ButtonContent(
                    text = text,
                    loading = loading,
                    icon = icon,
                    textColor = ErrorRed
                )
            }
        }
    }
}


@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    icon: ImageVector?,
    textColor: Color
) {

    if (loading) {

        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = textColor
        )

    } else {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            icon?.let {

                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = textColor
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
