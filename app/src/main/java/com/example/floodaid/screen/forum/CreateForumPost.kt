package com.example.floodaid.screen.forum

import BottomBar
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.floodaid.R
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForumPost(
    navController: NavHostController = rememberNavController(),
    onEvent: (ForumEvent) -> Unit,
    viewModel: ForumViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }) { paddingValues ->
        CreateForumPostScreen(paddingValues, navController,onEvent,state )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForumPostScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    onEvent: (ForumEvent) -> Unit,
    state: ForumState
) {
    // State for content in the TextField
    var forumContent by remember { mutableStateOf("") }
    var currentDistrict by remember { mutableStateOf("") }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Firestore reference
    val firestore = FirebaseFirestore.getInstance()

    // State to hold the Base64 string and Bitmap
    val userProfilePictureBase64 = remember { mutableStateOf<String?>(null) }
    val profileBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // State for selected images (using mutableStateListOf for a list of URIs)
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val imageBase64List = selectedImageUris.mapNotNull { uri -> uriToBase64(context, uri) }

    // This will hold the launcher for picking multiple images
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val availableSlots = 5 - selectedImageUris.size
                val urisToAdd = uris.take(availableSlots)
                selectedImageUris.addAll(urisToAdd) // Add the selected URIs
                if (uris.size > availableSlots) {
                    Toast.makeText(context, "Only 5 images allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Scaffold to manage UI structure
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Forum Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {

                            val forumPost = ForumPost(
                                id = UUID.randomUUID().toString(),
                                content = forumContent,
                                authorId = userId,
                                timestamp = System.currentTimeMillis(),
                                region = currentDistrict,
                                imageUrls = imageBase64List
                            )

                            // Trigger the SaveForumPost event with the forumPost data
                            onEvent(ForumEvent.SaveForumPost(forumPost))
                        },
                        enabled = (selectedImageUris.isNotEmpty() || forumContent.isNotBlank()) && currentDistrict.isNotBlank(),

                        ) {
                        Text("Post")
                    }


                })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(paddingValues) // outer scaffold
                .padding(innerPadding) // inner scaffold
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()) // Make the content scrollable
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                // Ensure the userId is not null
                if (userId != null) {
                    // Fetch user profile data from Firestore
                    LaunchedEffect(userId) {
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val base64String = document.getString("profilePictureBase64")
                                    userProfilePictureBase64.value = base64String

                                    // Decode the Base64 string into a Bitmap
                                    if (base64String != null) {
                                        profileBitmap.value = decodeBase64ToBitmap(base64String)
                                    }
                                }
                            }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display the profile picture or a default image if not available
                    if (profileBitmap.value != null) {
                        Image(
                            bitmap = profileBitmap.value!!.asImageBitmap(),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)

                        )
                    } else {
                        // Fallback: Show a default image if the profile picture is null or invalid
                        Image(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                        )
                    }
                    val districts = listOf(
                        "Hulu Langat",
                        "Ampang",
                        "Cheras",
                        "Semenyih",
                        "Kajang",
                        "Bangi",
                        "Hulu Selangor",
                        "Kuala Kubu Bharu",
                        "Serendah",
                        "Bukit Beruntung",
                        "Batang Kali",
                        "Ulu Yam"
                    )


                    // Container for Address Box

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        // Editable TextField for typing input
                        OutlinedTextField(
                            value = currentDistrict,
                            label = {
                                Text(
                                    text = "Place Of Incident",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = Color.Black
                                    )
                                )
                            },
                            singleLine = true,
                            onValueChange = {
                                currentDistrict = it
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = if (currentDistrict.isEmpty()) Color(0xFFBEC2C2) else Color.Black
                            ),
                            placeholder = {
                                Text(
                                    text = "Where did this happen?",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = AlegreyaSansFontFamily,
                                        color = Color.Black
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        )
                    }
                }

                // TextField for user input
                TextField(
                    value = forumContent,
                    placeholder = {
                        Text(
                            text = "What's happening?", style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = AlegreyaSansFontFamily,
                                color = Color.Gray
                            )
                        )
                    },
                    onValueChange = { forumContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 8.dp),
                    maxLines = 10,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFBEC2C2)
                    ),
                    textStyle = TextStyle(
                        fontSize = 24.sp, fontFamily = AlegreyaSansFontFamily, color = Color.Black
                    )
                )
                var selectedImageUriToShow by remember { mutableStateOf<Uri?>(null) }

                // LazyRow to display selected images
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(selectedImageUris.size) { index ->
                        Box(modifier = Modifier.clickable {
                            selectedImageUriToShow = selectedImageUris[index]
                        }) {
                            AsyncImage(
                                model = selectedImageUris[index],
                                contentDescription = null,
                                modifier = Modifier.size(200.dp)
                            )
                            IconButton(
                                onClick = { selectedImageUris.removeAt(index) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }
                if (selectedImageUriToShow != null) {
                    Dialog(onDismissRequest = { selectedImageUriToShow = null }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color.White)
                        ) {
                            AsyncImage(
                                model = selectedImageUriToShow,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }

            }

            // Button at the bottom to add more photos
            Button(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = selectedImageUris.size < 5,
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Align the button at the bottom
            ) {
                Text("Add Photo (${selectedImageUris.size}/5)")
            }
        }
    }
}

fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null // Return null if the decoding fails
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        bytes?.let {
            Base64.encodeToString(it, Base64.DEFAULT)
        }
    } catch (e: Exception) {
        null
    }
}
