package com.example.floodaid.screen.profile

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.floodaid.R
import com.example.floodaid.models.UserProfile
import com.example.floodaid.screen.login.GenderSelector
import com.example.floodaid.screen.login.StateSelector
import com.example.floodaid.screen.login.datePickerFieldToModal
import com.example.floodaid.screen.login.saveProfileToFirebaseAndLocal
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.screen.volunteer.checkIfUserIsVolunteer
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.jetpackcomposeauthui.components.CTextField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Profile(
    navController: NavHostController,
    viewModel: ProfileViewModel,
    volunteerViewModel: VolunteerViewModel
) {
    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val painter = rememberAsyncImagePainter(
        model = imageUri.value ?: R.drawable.ic_user,
        error = painterResource(R.drawable.ic_user)
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }
    val profileState by viewModel.profile.collectAsState()
    val registration by volunteerViewModel.volunteer.collectAsState()

    val focusManager = LocalFocusManager.current
    val phoneFocusRequester = remember  { FocusRequester() }
    val emgNameFocusRequester = remember  { FocusRequester() }
    val emgNumFocusRequester = remember  { FocusRequester() }

    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var myKadOrPassport by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var birthOfDate by rememberSaveable { mutableStateOf("") }
    var currentDistrict by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }

    var isProfileInitialized by rememberSaveable { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var isRegistered by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isRegistered = checkIfUserIsVolunteer(userId)
        }
    }

    LaunchedEffect(profileState) {
        if (!isProfileInitialized && profileState != null) {
            val profile = profileState // local val enables smart cast
            if (profile != null) {
                fullName = profile.fullName
                username = profile.userName
                myKadOrPassport = profile.myKadOrPassport
                selectedGender = profile.gender
                birthOfDate = profile.birthOfDate
                currentDistrict = profile.location
                imageUrl = profile.profilePictureUrl
            }
            isProfileInitialized = true
        }
    }


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
                            AsyncImage(
                                model = imageUri.value ?: imageUrl,
                                error = painterResource(R.drawable.ic_user),
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
                        hint = "MyKad / Passport Number",
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

                    if (isRegistered) {
                        val white = Color.White
                        val semiWhite = Color.White.copy(alpha = 0.7f)

                        OutlinedTextField(
                            value = registration.phoneNum,
                            onValueChange = volunteerViewModel::updatePhoneNumber,
                            label = {
                                Text(
                                    "Your Phone Number",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { emgNameFocusRequester.requestFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(phoneFocusRequester),
                            isError = !volunteerViewModel.validatePhoneNumber(registration.phoneNum),
                            supportingText = {
                                if (!volunteerViewModel.validatePhoneNumber(registration.phoneNum)) {
                                    Text("Please enter a valid phone number", color = white)
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )

                        OutlinedTextField(
                            value = registration.emgContact,
                            onValueChange = volunteerViewModel::updateEmergencyContact,
                            label = {
                                Text(
                                    "Emergency Contact Name",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { emgNumFocusRequester.requestFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(emgNameFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )

                        OutlinedTextField(
                            value = registration.emgNum,
                            onValueChange = volunteerViewModel::updateEmergencyPhone,
                            label = {
                                Text(
                                    "Emergency Contact Phone",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .focusRequester(emgNumFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            supportingText = {
                                if (!volunteerViewModel.validatePhoneNumber(registration.emgNum)) {
                                    Text("Please enter a valid phone number", color = white)
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )
                    }


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

                                                    volunteerViewModel.submitRegistration(uid)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "Firebase",
                                                    "Image upload failed: ${e.message}"
                                                )
                                                isUploading = false
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
                                        )

                                        saveProfileToFirebaseAndLocal(
                                            profile,
                                            navController,
                                            context
                                        )

                                        volunteerViewModel.submitRegistration(uid)
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
                                    "Update Profile",
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
                            AsyncImage(
                                model = imageUri.value ?: imageUrl,
                                error = painterResource(R.drawable.ic_user),
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
                        hint = "MyKad / Passport Number",
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

                    if (isRegistered) {
                        val white = Color.White
                        val semiWhite = Color.White.copy(alpha = 0.7f)

                        OutlinedTextField(
                            value = registration.phoneNum,
                            onValueChange = volunteerViewModel::updatePhoneNumber,
                            label = {
                                Text(
                                    "Your Phone Number",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { emgNameFocusRequester.requestFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(phoneFocusRequester),
                            isError = !volunteerViewModel.validatePhoneNumber(registration.phoneNum),
                            supportingText = {
                                if (!volunteerViewModel.validatePhoneNumber(registration.phoneNum)) {
                                    Text("Please enter a valid phone number", color = white)
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )

                        OutlinedTextField(
                            value = registration.emgContact,
                            onValueChange = volunteerViewModel::updateEmergencyContact,
                            label = {
                                Text(
                                    "Emergency Contact Name",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { emgNumFocusRequester.requestFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(emgNameFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )

                        OutlinedTextField(
                            value = registration.emgNum,
                            onValueChange = volunteerViewModel::updateEmergencyPhone,
                            label = {
                                Text(
                                    "Emergency Contact Phone",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = semiWhite
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .focusRequester(emgNumFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = white,
                                unfocusedIndicatorColor = semiWhite,
                                cursorColor = white,
                                focusedLabelColor = white,
                                unfocusedLabelColor = semiWhite
                            ),
                            supportingText = {
                                if (!volunteerViewModel.validatePhoneNumber(registration.emgNum)) {
                                    Text("Please enter a valid phone number", color = white)
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = white
                            )
                        )
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp)
                    ) {
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

                                                    volunteerViewModel.submitRegistration(uid)
                                                }


                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(
                                                    "Firebase",
                                                    "Image upload failed: ${e.message}"
                                                )
                                                isUploading = false
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
                                        )

                                        saveProfileToFirebaseAndLocal(
                                            profile,
                                            navController,
                                            context
                                        )

                                        volunteerViewModel.submitRegistration(uid)
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
                                    "Update Profile",
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