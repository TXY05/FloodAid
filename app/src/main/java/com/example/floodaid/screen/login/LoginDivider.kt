package com.example.floodaid.screen.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginDivider(text: String) {
    Row(
        modifier= Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            thickness = 1.dp,
            color = Color.Gray
        )

        Text(text=text,
            modifier= Modifier.padding(8.dp),
            fontSize = 18.sp,
            color = Color.White)

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}