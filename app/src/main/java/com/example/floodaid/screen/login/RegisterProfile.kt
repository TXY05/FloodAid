package com.example.floodaid.screen.login

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.example.floodaid.roomDatabase.database.FloodAidDatabase
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.jetpackcomposeauthui.components.CTextField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun RegisterProfile(navController: NavHostController) {
    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val painter = rememberAsyncImagePainter(
        model = imageUri.value ?: R.drawable.ic_user,
        error = painterResource(R.drawable.ic_user)
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }

    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var myKadOrPassport by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var birthOfDate by rememberSaveable { mutableStateOf("") }
    var currentDistrict by rememberSaveable { mutableStateOf("") }

    // Move context-related tasks to a Composable scope
    val context = LocalContext.current


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            // Background Image
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
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
                                .clickable { launcher.launch("image/*") }
                        ) {
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
                        onValueChange = { fullName = it },
                        scaleDown = .5f
                    )
                    CTextField(
                        hint = "Username",
                        value = username,
                        onValueChange = { username = it },
                        scaleDown = .5f
                    )
                    CTextField(
                        hint = "Mykad / Passport Number",
                        value = myKadOrPassport,
                        onValueChange = { myKadOrPassport = it }, scaleDown = .5f
                    )
                    GenderSelector(
                        selectedGender = selectedGender,
                        onGenderSelected = { selectedGender = it },
                        scaleDown = .5f
                    )
                    datePickerFieldToModal(
                        birthOfDate = birthOfDate,
                        onDateSelected = { birthOfDate = it }, scaleDown = .5f
                    )
                    StateSelector(
                        modifier = Modifier.fillMaxWidth(),
                        currentDistrict = currentDistrict,
                        onDistrictSelected = { currentDistrict = it },
                        onTextChanged = { currentDistrict = it },
                        scaleDown = 0.5f
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        var isUploading by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                val email = FirebaseAuth.getInstance().currentUser?.email
                                isUploading = true
                                if (uid != null) {
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val fileName = "$uid-profile.jpg"
                                    val imageRef = storageRef.child("profileImages/$fileName")

                                    if (imageUri.value != null) {
                                        // Upload image to Firebase Storage
                                        imageRef.putFile(imageUri.value!!)
                                            .addOnSuccessListener {
                                                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                    // ✅ Create profile with image URL
                                                    val profile = UserProfile(
                                                        uid = uid,
                                                        fullName = fullName,
                                                        userName = username,
                                                        email = email ?: "",
                                                        myKadOrPassport = myKadOrPassport,
                                                        gender = selectedGender,
                                                        birthOfDate = birthOfDate,
                                                        location = currentDistrict,
                                                        profilePictureUrl = downloadUrl.toString()
                                                    )

                                                    saveProfileToFirebaseAndLocal(
                                                        profile,
                                                        navController,
                                                        context
                                                    )
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "Firebase",
                                                    "Image upload failed: ${e.message}"
                                                )
                                            }
                                    } else {
                                        // No image selected, save profile with empty image
                                        val profile = UserProfile(
                                            uid = uid,
                                            fullName = fullName,
                                            userName = username,
                                            email = email ?: "",
                                            myKadOrPassport = myKadOrPassport,
                                            gender = selectedGender,
                                            birthOfDate = birthOfDate,
                                            location = currentDistrict,
                                            profilePictureUrl = ""
                                        )

                                        saveProfileToFirebaseAndLocal(
                                            profile,
                                            navController,
                                            context
                                        )
                                    }
                                }


                            },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(0.5f)
                                .height(52.dp)
                                .align(alignment = Alignment.BottomCenter),
                            enabled = !isUploading && fullName.isNotBlank() && username.isNotBlank() && myKadOrPassport.isNotBlank()
                                    && selectedGender.isNotBlank() && birthOfDate.isNotBlank() && currentDistrict.isNotBlank()
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
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
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            // Background Image
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
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
                                .clickable { launcher.launch("image/*") }
                        ) {
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
                        onValueChange = { fullName = it })
                    CTextField(
                        hint = "Username",
                        value = username,
                        onValueChange = { username = it })
                    CTextField(
                        hint = "Mykad / Passport Number",
                        value = myKadOrPassport,
                        onValueChange = { myKadOrPassport = it })
                    GenderSelector(
                        selectedGender = selectedGender,
                        onGenderSelected = { selectedGender = it })
                    datePickerFieldToModal(
                        birthOfDate = birthOfDate,
                        onDateSelected = { birthOfDate = it },
                        scaleDown = 1f
                    )
                    StateSelector(
                        modifier = Modifier.fillMaxWidth(),
                        currentDistrict = currentDistrict,
                        onDistrictSelected = { currentDistrict = it },
                        onTextChanged = { currentDistrict = it }
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        var isUploading by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                val email = FirebaseAuth.getInstance().currentUser?.email
                                isUploading = true
                                if (uid != null) {
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val fileName = "$uid-profile.jpg"
                                    val imageRef = storageRef.child("profileImages/$fileName")

                                    if (imageUri.value != null) {
                                        // Upload image to Firebase Storage
                                        imageRef.putFile(imageUri.value!!)
                                            .addOnSuccessListener {
                                                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                    // ✅ Create profile with image URL
                                                    val profile = UserProfile(
                                                        uid = uid,
                                                        fullName = fullName,
                                                        userName = username,
                                                        email = email ?: "",
                                                        myKadOrPassport = myKadOrPassport,
                                                        gender = selectedGender,
                                                        birthOfDate = birthOfDate,
                                                        location = currentDistrict,
                                                        profilePictureUrl = downloadUrl.toString()
                                                    )

                                                    saveProfileToFirebaseAndLocal(
                                                        profile,
                                                        navController,
                                                        context
                                                    )
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "Firebase",
                                                    "Image upload failed: ${e.message}"
                                                )
                                            }
                                    } else {
                                        // No image selected, save profile with empty image
                                        val profile = UserProfile(
                                            uid = uid,
                                            fullName = fullName,
                                            userName = username,
                                            email = email ?: "",
                                            myKadOrPassport = myKadOrPassport,
                                            gender = selectedGender,
                                            birthOfDate = birthOfDate,
                                            location = currentDistrict,
                                            profilePictureUrl = ""
                                        )

                                        saveProfileToFirebaseAndLocal(
                                            profile,
                                            navController,
                                            context
                                        )
                                    }
                                }


                            },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                                .height(52.dp)
                                .align(alignment = Alignment.BottomCenter),
                            enabled = !isUploading && fullName.isNotBlank() && username.isNotBlank() && myKadOrPassport.isNotBlank()
                                    && selectedGender.isNotBlank() && birthOfDate.isNotBlank() && currentDistrict.isNotBlank()
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
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
    }
}

@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    scaleDown: Float = 1f,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth(scaleDown)
            .height(56.dp) // Set height to ensure proper alignment
            .clickable { showDialog = true }
    ) {
        Text(
            text = if (selectedGender.isEmpty()) "Gender" else selectedGender,
            fontSize = 18.sp,
            fontFamily = AlegreyaSansFontFamily,
            color = if (selectedGender.isEmpty()) Color(0xFFBEC2C2) else Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart) // Align the text to the start of the box
                .padding(start = 16.dp) // Ensure text doesn't go outside box bounds
        )

        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(1.dp)
                .background(Color(0xFFBEC2C2))
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
                            text = gender,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGenderSelected(gender)
                                    showDialog = false
                                }
                                .padding(12.dp),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily
                            )
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun StateSelector(
    modifier: Modifier = Modifier,
    currentDistrict: String,
    onDistrictSelected: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    scaleDown: Float = 1f,
) {
    val districts = listOf(
        "Hulu Langat", "Ampang", "Cheras", "Semenyih", "Kajang", "Bangi", "Hulu Selangor",
        "Kuala Kubu Bharu", "Serendah", "Bukit Beruntung", "Batang Kali", "Ulu Yam"
    )

    var expanded by remember { mutableStateOf(false) }

    // Wrap in Column to center content horizontally
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fixed width Box to keep alignment
        Box(modifier = Modifier.fillMaxWidth(scaleDown)) {
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
                    .clickable { expanded = true }
                    .padding(vertical = 8.dp),
            )
        }

        // Dropdown below
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(scaleDown)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 150.dp)
                ) {
                    items(
                        districts.filter {
                            it.lowercase().contains(currentDistrict.lowercase())
                        }.sorted()
                    ) { district ->
                        DistrictItems(title = district) {
                            onDistrictSelected(district)
                            expanded = false
                        }
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
fun datePickerFieldToModal(
    birthOfDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    scaleDown: Float = 1f,
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    TextField(
        value = birthOfDate,
        onValueChange = {},
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
            .fillMaxWidth(scaleDown)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)?.let {
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
            onDateSelected = { millis ->
                if (millis != null) {
                    onDateSelected(convertMillisToDate(millis))
                }
            },
            onDismiss = { showModal = false },
            scaleDown
        )
    }
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
    scaleDown: Float,
) {
    val datePickerState = rememberDatePickerState()
    var calenderScale by rememberSaveable { mutableFloatStateOf(0f) }
    if (scaleDown < 1f) calenderScale = scaleDown + 0.35f
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(calenderScale)
        ) {
            DatePicker(state = datePickerState)
        }


    }
}


fun saveProfileToFirebaseAndLocal(
    profile: UserProfile,
    navController: NavHostController,
    context: Context,
) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(profile.uid)
        .set(profile)
        .addOnSuccessListener {
            Log.d("Profile", "Profile saved successfully")
            navController.navigate(Screen.Dashboard.route)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error saving profile", e)
        }

    val db = FloodAidDatabase.getInstance(context)
    val userProfileDao = db.userProfileDao()

    CoroutineScope(Dispatchers.IO).launch {
        userProfileDao.insert(profile)
    }
}
