package com.example.floodaid.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    error: String? = null // Add an optional error parameter
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = hint,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = Color(0xFFBEC2C2)
                    )
                )
            },
            placeholder = {
                Text(
                    text = hint,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = Color(0xFFBEC2C2)
                    )
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFFBEC2C2),
                unfocusedIndicatorColor = Color(0xFFBEC2C2),
                cursorColor = Color(0xFFBEC2C2)
            ),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontFamily = AlegreyaSansFontFamily,
                color = Color.White
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon =
                    if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (isPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = description, tint = Color.White)
                }
            },
        )

        // Display error message below the password field if there is an error
        if (!error.isNullOrEmpty()) {
            Text(
                text = error,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Red
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

