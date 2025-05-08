package com.example.floodaid.screen.profile

import BottomBar
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.floodaid.R
import com.example.floodaid.composable.TopBar
import com.example.floodaid.models.UserProfile
import com.example.floodaid.screen.login.GenderSelector
import com.example.floodaid.screen.login.StateSelector
import com.example.floodaid.screen.login.datePickerFieldToModal
import com.example.floodaid.screen.login.saveProfileToFirebaseAndLocal
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.jetpackcomposeauthui.components.CTextField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Profile(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }

    val profileState by viewModel.profile.collectAsState()

    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var myKadOrPassport by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var birthOfDate by rememberSaveable { mutableStateOf("") }
    var currentDistrict by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(profileState) {
        profileState?.let { profile ->
            fullName = profile.fullName
            username = profile.userName
            myKadOrPassport = profile.myKadOrPassport
            selectedGender = profile.gender
            birthOfDate = profile.birthOfDate
            currentDistrict = profile.location
            imageUrl = profile.profilePictureUrl
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
                                model = imageUrl,
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
                                            profilePictureUrl = imageUrl
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
                                model = imageUrl,
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
                                            profilePictureUrl = imageUrl
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