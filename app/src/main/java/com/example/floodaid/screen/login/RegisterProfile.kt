package com.example.floodaid.screen.login

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.floodaid.R
import com.example.floodaid.models.Screen
import com.example.floodaid.models.UserProfile
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.jetpackcomposeauthui.components.CTextField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegisterProfile(modifier: Modifier = Modifier, navController: NavHostController) {
    val imageUri = rememberSaveable { mutableStateOf("") }
    val painter = rememberAsyncImagePainter(
        model = if (imageUri.value.isEmpty()) {
            R.drawable.ic_user
        } else {
            imageUri.value
        }, error = painterResource(R.drawable.ic_user)
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri.value = it.toString()
        }
    }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var myKadOrPassport by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var birthOfDate by remember { mutableStateOf("") }
    var currentDistrict by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        /// Background Image
        Image(
            painter = painterResource(
                id = R.drawable.loginbackground
            ),
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
                Box(
                    modifier = Modifier
                        .padding(top = 50.dp, bottom = 20.dp)
                        .size(90.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    // Profile Image
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { launcher.launch("image/*") }) {
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .offset(x = 10.dp, y = 10.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Camera Icon",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                        )
                    }
                }

                Text(
                    text = "Change Profile Picture",
                    color = Color.White,
                    fontFamily = AlegreyaSansFontFamily,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 15.dp)
                )


                CTextField(
                    hint = "Full Name",
                    value = fullName,
                    onValueChange = { newValue -> fullName = newValue })

                CTextField(
                    hint = "Username",
                    value = username,
                    onValueChange = { newValue -> username = newValue })

                CTextField(
                    hint = "Mykad / Passport Number",
                    value = myKadOrPassport,
                    onValueChange = { newValue -> myKadOrPassport = newValue })

                GenderSelector(
                    selectedGender = selectedGender, onGenderSelected = { selectedGender = it })


                birthOfDate=DatePickerFieldToModal()

                StateSelector(
                    modifier = Modifier.fillMaxWidth(),
                    currentDistrict = currentDistrict,
                    onDistrictSelected = { district -> currentDistrict = district },
                    onTextChanged = { newText -> currentDistrict = newText }
                )



                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            val email = FirebaseAuth.getInstance().currentUser?.email

                            if (uid != null) {
                                val profile = UserProfile(
                                    uid = uid,
                                    fullName = fullName,       // get these values from your TextFields
                                    userName = username,
                                    email = email.toString(),
                                    myKadOrPassport = myKadOrPassport,
                                    gender = selectedGender,
                                    birthOfDate = birthOfDate, // format: "YYYY-MM-DD"
                                    location = currentDistrict
                                )

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .set(profile)
                                    .addOnSuccessListener {
                                        Log.d("Profile", "Profile saved")

                                        navController.navigate(Screen.Dashboard.route)
                                    }
                                    .addOnFailureListener {
                                        Log.e("Profile", "Error saving profile", it)
                                        // Show error message
                                    }
                            }

                        },
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                            .height(52.dp)
                            .align(alignment = Alignment.BottomCenter)


                    ) {
                        Text(
                            "Save Profile",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                fontWeight = FontWeight(500),
                                color = Color.White
                            )
                        )


                    }
                }
            }
        }
    }

}

@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp)) {
        Text(
            text = if (selectedGender.isEmpty()) "Gender" else selectedGender,
            fontSize = 18.sp,
            fontFamily = AlegreyaSansFontFamily,
            color = if (selectedGender.isEmpty()) Color(0xFFBEC2C2) else Color.White,
            modifier = Modifier.padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),

            )

        // Bottom border only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .height(1.dp) // Bottom border thickness
                .background(Color(0xFFBEC2C2)) // Border color
        )
    }

    // Popup dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Gender") },
            text = {
                Column {
                    listOf("Male", "Female", "Other").forEach { gender ->
                        Text(
                            text = gender, modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGenderSelected(gender)
                                    showDialog = false
                                }
                                .padding(12.dp), style = TextStyle(
                                fontSize = 18.sp, fontFamily = AlegreyaSansFontFamily
                            )
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {})
    }
}



@Composable
fun StateSelector(
    modifier: Modifier = Modifier,
    currentDistrict: String,
    onDistrictSelected: (String) -> Unit,
    onTextChanged: (String) -> Unit,
) {
    val districts = listOf(
        "Hulu Langat", "Ampang", "Cheras", "Semenyih", "Kajang", "Bangi", "Hulu Selangor",
        "Kuala Kubu Bharu", "Serendah", "Bukit Beruntung", "Batang Kali", "Ulu Yam"
    )

    var expanded by remember { mutableStateOf(false) }

    // Container for Address Box
    Box(modifier = modifier.fillMaxWidth()) {
        // Editable TextField for typing input
        TextField(
            value = currentDistrict,
            label = {
                Text(
                    text = "State",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = Color(0xFFBEC2C2)
                    )
                )
            },
            onValueChange = {
                onTextChanged(it)
                expanded = it.isNotEmpty()
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontFamily = AlegreyaSansFontFamily,
                color = if (currentDistrict.isEmpty()) Color(0xFFBEC2C2) else Color.White
            ),
            placeholder = {
                Text(
                    text = "State",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = Color(0xFFBEC2C2)
                    )
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFFBEC2C2),
                unfocusedIndicatorColor = Color(0xFFBEC2C2),
                cursorColor = Color(0xFFBEC2C2)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = true
                } // Expand the dropdown
                .padding(vertical = 8.dp),
        )


    }

    // Displaying dropdown list (AnimatedVisibility for the dropdown)
    AnimatedVisibility(visible = expanded) {
        Card(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .fillMaxWidth() // Fill max width of parent container
                .widthIn(max = 280.dp), // Limit maximum width to avoid overflow
            shape = RoundedCornerShape(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 150.dp) // Limit max height of the list
            ) {
                items(
                    districts.filter {
                        it.lowercase()
                            .contains(currentDistrict.lowercase()) // Filter based on input
                    }.sorted()
                ) { district ->
                    DistrictItems(title = district) {
                        onDistrictSelected(district) // Set selected district
                        expanded = false // Close dropdown after selection
                    }
                }
            }
        }
    }
}

@Composable
fun DistrictItems(
    title: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(title) } // Handle item selection
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}


@Composable
fun DatePickerFieldToModal(modifier: Modifier = Modifier): String {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    TextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        placeholder = {
            Text(
                text = "MM/DD/YYYY",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    color = Color(0xFFBEC2C2)
                )
            )
        },

        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                    if (upEvent != null) {
                        showModal = true
                    }
                }
            },
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

    if (showModal) {
        DatePickerModal(
            onDateSelected = { selectedDate = it },
            onDismiss = { showModal = false }
        )
    }
    return selectedDate?.let { convertMillisToDate(it) } ?: ""
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}