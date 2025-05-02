package com.example.floodaid.screen.login

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.floodaid.R
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.AlegreyaFontFamily
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.AuthState
import com.example.floodaid.viewmodel.AuthViewModel
import com.example.jetpackcomposeauthui.components.DontHaveAccountRow

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Welcome(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate(Screen.Dashboard.route)

            is AuthState.Error -> Toast.makeText(
                context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Image(
            painter = painterResource(id = R.drawable.welcomebackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .width(320.dp)
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "WELCOME",
                textAlign = TextAlign.Center,
                fontSize = 55.sp,
                fontFamily = AlegreyaFontFamily,
                fontWeight = FontWeight(500),
                color = Color.White
            )

            Text(
                "Stay safe. Stay connected.\n" +
                        "FloodAid is here for you.",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontFamily = AlegreyaSansFontFamily,
                fontWeight = FontWeight(500),
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(4f))

            Button(
                onClick = {
                    navController.navigate("login"){
                        popUpTo("welcome") {
                            inclusive = false
                        }
                    }
                },
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Sign In With Email",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        fontWeight = FontWeight(500),
                        color = Color.White
                    )
                )


            }

            DontHaveAccountRow(
                onSignupTap = {
                    navController.navigate("signup"){
                        popUpTo("welcome") {
                            inclusive = false
                        }
                    }
                }
            )
        }
    }
}
