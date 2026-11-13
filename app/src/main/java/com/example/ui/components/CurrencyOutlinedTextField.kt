package com.example.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CurrencyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String = "CLP",
    isCurrency: Boolean = true,
    placeholder: String = "0",
    helperText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Filter digits only for integers, or digits and one decimal point for decimals
            val filtered = if (isCurrency) {
                newValue.filter { it.isDigit() }
            } else {
                newValue.filter { it.isDigit() || it == '.' }
            }
            onValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = if (isCurrency) {
            { Text("$ ", color = MaterialTheme.colorScheme.primary) }
        } else null,
        suffix = { Text(suffix, style = MaterialTheme.typography.bodySmall) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar campo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        supportingText = helperText?.let {
            { Text(it, style = MaterialTheme.typography.labelSmall) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isCurrency) KeyboardType.Number else KeyboardType.Decimal
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
    )
}
