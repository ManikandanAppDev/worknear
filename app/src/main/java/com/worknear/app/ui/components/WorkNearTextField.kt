package com.worknear.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.LightGray
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun WorkNearTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                color = LightGray,
                fontFamily = sansProText
            )
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MediumGray
                )
            }
        },
        trailingIcon = trailingIcon?.let { icon ->
            {
                IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MediumGray
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderGray,
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            cursorColor = PrimaryBlue
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = sansProText)
    )
}
