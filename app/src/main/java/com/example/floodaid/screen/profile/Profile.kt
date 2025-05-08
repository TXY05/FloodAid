package com.example.floodaid.screen.profile

import BottomBar
import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.CircleShape
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

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var myKadOrPassport by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var birthOfDate by remember { mutableStateOf("") }
    var currentDistrict by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        profileState?.let { profile ->
            fullName = profile.fullName
            username = profile.userName
            myKadOrPassport = profile.myKadOrPassport
            selectedGender = profile.gender
            birthOfDate = profile.birthOfDate
            currentDistrict = profile.location
            imageUri.value = profile.profilePictureUrl.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
        }
    }

    var isEditingFullName by remember { mutableStateOf(false) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var isEditingMyKad by remember { mutableStateOf(false) }
    var isEditingGender by remember { mutableStateOf(false) }
    var isEditingBirthDate by remember { mutableStateOf(false) }
    var isEditingDistrict by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

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
                            model = imageUri.value ?: R.drawable.ic_user,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_user),
                            placeholder = painterResource(id = R.drawable.ic_user)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingFullName) {
                        CTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            hint = "Full Name"
                        )
                    } else {
                        Text(
                            text = "Full Name: $fullName",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingFullName = !isEditingFullName }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Full Name")
                    }
                }

                // Username
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingUsername) {
                        CTextField(
                            value = username,
                            onValueChange = { username = it },
                            hint = "Username"
                        )
                    } else {
                        Text(
                            text = "Username: $username",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingUsername = !isEditingUsername }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Username")
                    }
                }

// MyKad / Passport Number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingMyKad) {
                        CTextField(
                            value = myKadOrPassport,
                            onValueChange = { myKadOrPassport = it },
                            hint = "Mykad / Passport Number"
                        )
                    } else {
                        Text(
                            text = "Mykad / Passport Number: $myKadOrPassport",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingMyKad = !isEditingMyKad }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Mykad / Passport Number")
                    }
                }

// Gender
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingGender) {
                        GenderSelector(
                            selectedGender = selectedGender,
                            onGenderSelected = { selectedGender = it }
                        )
                    } else {
                        Text(
                            text = "Gender: $selectedGender",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingGender = !isEditingGender }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Gender")
                    }
                }

// Date of Birth
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingBirthDate) {
                        birthOfDate = datePickerFieldToModal()
                    } else {
                        Text(
                            text = "Date: $birthOfDate",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingBirthDate = !isEditingBirthDate }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Date")
                    }
                }

// District
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isEditingDistrict) {
                        StateSelector(
                            modifier = Modifier.weight(1f),
                            currentDistrict = currentDistrict,
                            onDistrictSelected = { currentDistrict = it },
                            onTextChanged = { currentDistrict = it }
                        )
                    } else {
                        Text(
                            text = "District: $currentDistrict",
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { isEditingDistrict = !isEditingDistrict }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit District")
                    }
                }


                Box(modifier = Modifier.fillMaxSize()) {

                    Button(
                        onClick = {
                            Log.d("DEBUG", "Update button clicked")
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            val email = FirebaseAuth.getInstance().currentUser?.email
                            isUploading = true
                            if (uid != null) {
                                val storageRef = FirebaseStorage.getInstance().reference
                                val fileName = "$uid-profile.jpg"
                                val imageRef = storageRef.child("profileImages/$fileName")

                                val uploadAndSaveProfile: (String) -> Unit = { imageUrl ->
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
                                    viewModel.updateProfile(profile)
                                    isUploading = false
                                    navController.popBackStack()
                                    navController.navigate("profile")
                                }

                                if (imageUri.value != null && imageUri.value.toString().startsWith("content://")) {
                                    // A new image has been picked
                                    imageRef.putFile(imageUri.value!!)
                                        .addOnSuccessListener {
                                            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                uploadAndSaveProfile(downloadUrl.toString())
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firebase", "Image upload failed: ${e.message}")
                                            isUploading = false
                                        }
                                } else {
                                    // No new image selected, keep existing URL (or empty if null)
                                    uploadAndSaveProfile(profileState?.profilePictureUrl ?: "")
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