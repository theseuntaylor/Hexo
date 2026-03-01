package com.theseuntaylor.hexo.core.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.theseuntaylor.hexo.core.TextFieldState
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.core.theme.md_theme_dark_secondary
import com.theseuntaylor.hexo.core.theme.md_theme_light_onPrimaryContainer

@Composable
fun TextFieldError(textError: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textError,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun HexoTextField(
    modifier: Modifier = Modifier,
    label: Int,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    textFieldState: TextFieldState = remember {
        TextFieldState(
            validator = {
                it.isEmpty() && it.length > 3
            }
        )
    },
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    val displayValue = if (value.isNotEmpty()) value else textFieldState.text
    TextField(
        value = displayValue,
        onValueChange = {
            textFieldState.text = it
            onValueChange(it)
        },
        label = {
            Text(
                text = stringResource(id = label),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                textFieldState.onFocusChange(focusState.isFocused)
                if (!focusState.isFocused) {
                    textFieldState.enableShowErrors()
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = md_theme_dark_secondary,
            focusedContainerColor = md_theme_dark_secondary,
            unfocusedTextColor = md_theme_light_onPrimaryContainer,
            focusedTextColor = md_theme_light_onPrimaryContainer,
            unfocusedLabelColor = md_theme_dark_primary,
        ),
        isError = textFieldState.showErrors(),
        supportingText = {
            textFieldState.getError()?.let { error -> TextFieldError(textError = error) }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = imeAction,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeAction()
            }
        ),
    )
}