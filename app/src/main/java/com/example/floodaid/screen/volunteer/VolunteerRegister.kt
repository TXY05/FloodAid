package com.example.floodaid.screen.volunteer

import BottomBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.floodaid.composable.VButton
import com.example.floodaid.composable.VolunteerTopBar
import com.example.floodaid.models.Screen
import com.example.floodaid.screen.profile.ProfileViewModel
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerRegister(
    navController: NavHostController,
    viewModel: VolunteerViewModel,
    profileViewModel: ProfileViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val registration by viewModel.volunteer.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val focusManager = LocalFocusManager.current
    val phoneFocusRequester = remember  { FocusRequester() }
    val emgNameFocusRequester = remember  { FocusRequester() }
    val emgNumFocusRequester = remember  { FocusRequester() }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            VolunteerTopBar(
                scrollBehavior = scrollBehavior,
                navController = navController,
                onHistoryClick = { navController.navigate("volunteerHistory") },
                viewModel = profileViewModel
            )
        },
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addVolunteerEvent") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize().padding(horizontal = 40.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item{
                Text(
                    text = "Volunteer Registration",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item{
                OutlinedTextField(
                    value = registration.phoneNum,
                    onValueChange = viewModel::updatePhoneNumber,
                    label = { Text("Your Phone Number",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = AlegreyaSansFontFamily,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { emgNameFocusRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(phoneFocusRequester),
                    isError = !viewModel.validatePhoneNumber(registration.phoneNum),
                    supportingText = {
                        if (!viewModel.validatePhoneNumber(registration.phoneNum)) {
                            Text("Please enter a valid phone number")
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = colorScheme.onSurface
                    )
                )
            }

            item{
                OutlinedTextField(
                    value = registration.emgContact,
                    onValueChange = viewModel::updateEmergencyContact,
                    label = { Text("Emergency Contact Name",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = AlegreyaSansFontFamily,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { emgNumFocusRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emgNameFocusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = colorScheme.onSurface
                    )
                )
            }

            item{
                OutlinedTextField(
                    value = registration.emgNum,
                    onValueChange = viewModel::updateEmergencyPhone,
                    label = { Text("Emergency Contact Phone",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = AlegreyaSansFontFamily,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emgNumFocusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = colorScheme.primary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                    supportingText = {
                        if (!viewModel.validatePhoneNumber(registration.emgNum)) {
                            Text("Please enter a valid phone number")
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = colorScheme.onSurface
                    )
                )
            }

            item{
                VButton(
                    onClick = {
                        viewModel.submitRegistration(userId)
                        navController.popBackStack()
                        navController.navigate("volunteer_main")
                    },
                    text = "Register as Volunteer",
                    enabled = registration.phoneNum.isNotEmpty() &&
                            registration.emgContact.isNotEmpty() &&
                            registration.emgNum.isNotEmpty()
                )
            }
        }
    }
}