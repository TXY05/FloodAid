package com.example.jetpackcomposeauthui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    hint: String,
    error: String? = null,
    scaleDown: Float = 1f,
) {
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
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(scaleDown)
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
            )
        )

        // Show error message if error is not null or empty
        error?.let {
            Text(
                text = it,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Red
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

