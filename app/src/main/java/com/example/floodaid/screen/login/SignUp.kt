package com.example.floodaid.screen.login

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Signup(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val firestore = FirebaseFirestore.getInstance()

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
            passwordError = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val isComplete = doc.exists() && doc.getString("fullName") != null
                            if (isComplete) {
                                navController.navigate(Screen.Dashboard.route)
                            } else {
                                navController.navigate(Screen.RegisterProfile.route)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }

            is AuthState.Error -> Toast.makeText(
                context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }
    if (isLandscape) {
        Surface(
            color = Color(0xFF253334),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.loginbackground),
                    contentDescription = null,
                    modifier = Modifier
                        .height(800.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillBounds
                )

                // Content
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
                            "Create an Account",
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


                        // Email field with error message handling
                        CTextField(
                            hint = "Email Address",
                            value = email,
                            onValueChange = { newValue -> email = newValue },
                            error = emailError,
                            scaleDown = 0.5f
                        )

                        // Password field with error message handling
                        PasswordTextField(
                            hint = "Password",
                            value = password,
                            onValueChange = { newValue -> password = newValue },
                            error = passwordError,
                            scaleDown = 0.5f
                        )

                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Register Button
                        CButton(
                            text = "Register", onClick = {
                                if (validateInputs()) {
                                    authViewModel.signupFunction(email, password)
                                }
                            }, enabled = authState.value != AuthState.Loading,
                            scaleDown = 0.5f
                        )

                        LoginDivider(text = "or", scaleDown = 0.5f)

                        // Google Sign-in Button
                        OutlinedButton(
                            onClick = {
                                authViewModel.signInWithGoogle()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .fillMaxWidth(0.5f)
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

                        Row(
                            modifier = Modifier.padding(top = 12.dp, bottom = 52.dp)
                        ) {
                            Text(
                                "Already have an account?  ", style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = AlegreyaSansFontFamily,
                                    color = Color.White
                                )
                            )

                            Text(
                                "Sign In", style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = AlegreyaSansFontFamily,
                                    fontWeight = FontWeight(800),
                                    color = Color.White
                                ), modifier = Modifier.clickable {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Welcome.route) {
                                            saveState = false
                                        }
                                    }
                                })
                        }
                    }
                }
            }

        }
    } else {
        Surface(
            color = Color(0xFF253334),
            modifier = Modifier
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.loginbackground),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // Content
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
                            "Create an Account",
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


                        // Email field with error message handling
                        CTextField(
                            hint = "Email Address",
                            value = email,
                            onValueChange = { newValue -> email = newValue },
                            error = emailError
                        )

                        // Password field with error message handling
                        PasswordTextField(
                            hint = "Password",
                            value = password,
                            onValueChange = { newValue -> password = newValue },
                            error = passwordError
                        )

                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Register Button
                        CButton(
                            text = "Register", onClick = {
                                if (validateInputs()) {
                                    authViewModel.signupFunction(email, password)
                                }
                            }, enabled = authState.value != AuthState.Loading
                        )

                        LoginDivider(text = "or")

                        // Google Sign-in Button
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

                        Row(
                            modifier = Modifier.padding(top = 12.dp, bottom = 52.dp)
                        ) {
                            Text(
                                "Already have an account?  ", style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = AlegreyaSansFontFamily,
                                    color = Color.White
                                )
                            )

                            Text(
                                "Sign In", style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = AlegreyaSansFontFamily,
                                    fontWeight = FontWeight(800),
                                    color = Color.White
                                ), modifier = Modifier.clickable {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Welcome.route) {
                                            saveState = false
                                        }
                                    }
                                })
                        }
                    }
                }
            }

        }
    }
}
