package com.example.floodaid.screen.login

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.floodaid.R
import com.example.floodaid.composable.PasswordTextField
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.AlegreyaFontFamily
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.AuthState
import com.example.floodaid.viewmodel.AuthViewModel
import com.example.jetpackcomposeauthui.components.CButton
import com.example.jetpackcomposeauthui.components.CTextField
import com.example.jetpackcomposeauthui.components.DontHaveAccountRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Login(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value) {
        val state = authState.value

        if (!hasNavigated && state is AuthState.Authenticated) {
            hasNavigated = true

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val isComplete = doc.exists() && doc.getString("fullName") != null
                        if (isComplete) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.RegisterProfile.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                    .addOnFailureListener {
                        authViewModel.emitToast("Error loading profile")
                        hasNavigated = false
                    }
            }
        } else if (state is AuthState.Error) {
            authViewModel.emitToast(state.message)
            hasNavigated = false
        }
    }

    // Validation function
    fun validateInputs(): Boolean {
        var isValid = true
        emailError = null
        passwordError = null

        if (email.isEmpty()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters long"
            isValid = false
        }

        return isValid
    }

    Surface(
        color = Color(0xFF253334),
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            Image(
                painter = painterResource(id = R.drawable.loginbackground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .height(150.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "Hey there,",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = AlegreyaSansFontFamily,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                    )

                    Text(
                        "Welcome Back",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = AlegreyaFontFamily,
                            fontWeight = FontWeight(500),
                            color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                    )

                    // Text Fields
                    CTextField(
                        hint = "Email Address",
                        value = email,
                        onValueChange = { newValue -> email = newValue },
                        error = emailError
                    )

                    PasswordTextField(
                        hint = "Password",
                        value = password,
                        onValueChange = { newValue -> password = newValue },
                        error = passwordError
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CButton(
                        text = "Sign In",
                        onClick = {
                            if (validateInputs()) {
                                authViewModel.loginFunction(email, password)
                            }
                        },
                        enabled = authState.value != AuthState.Loading
                    )

                    LoginDivider(text = "or")

                    OutlinedButton(
                        onClick = {
                            authViewModel.signInWithGoogle()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google_icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Sign-in with Google",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                fontWeight = FontWeight(500),
                                color = Color.Black
                            )
                        )
                    }

                    DontHaveAccountRow(
                        onSignupTap = {
                            navController.navigate(Screen.Signup.route) {
                                popUpTo(Screen.Welcome.route) { saveState = false }
                            }
                        }
                    )
                }
            }
        }
    }
}
