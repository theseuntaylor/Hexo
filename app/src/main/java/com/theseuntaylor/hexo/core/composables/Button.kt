package com.theseuntaylor.hexo.core.composables

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theseuntaylor.hexo.core.theme.*

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        enabled = isEnabled
    ) {
        Text(
            text = text,
            style = Typography.labelLarge
        )
    }
}

@Preview
@Composable
fun ButtonPreview() {
    Button(text = "Press Me!", onClick = { /*TODO*/ })
}