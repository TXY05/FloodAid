package com.example.floodaid.screen.forum

import BottomBar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import androidx.compose.runtime.State
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEditor(
    navController: NavHostController = rememberNavController(),
    onEvent: (ForumEvent) -> Unit,
    viewModel: ForumViewModel,
) {
    val postToEdit by viewModel.postToEdit

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }) { paddingValues ->
        ForumPostEditorScreen(paddingValues, navController, onEvent, postToEdit, viewModel,postToEdit != null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumPostEditorScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    onEvent: (ForumEvent) -> Unit,
    forumPostToEdit: ForumPost? = null,
    viewModel: ForumViewModel,
    isEditMode: Boolean
) {

    // State for content in the TextField
    var forumContent by remember { mutableStateOf(forumPostToEdit?.content ?: "") }
    var currentDistrict by remember { mutableStateOf(forumPostToEdit?.region ?: "") }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    // State for selected images (using mutableStateListOf for a list of URIs)
    val selectedImageUris = remember {
        mutableStateListOf<Uri>().apply {
            forumPostToEdit?.imageUrls?.forEach { url ->
                if (url != null) {  // Check if the URL is not null
                    add(Uri.parse(url))
                }
            }
        }
    }


    val context = LocalContext.current

    // This will hold the launcher for picking multiple images
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val availableSlots = 5 - selectedImageUris.size
                val urisToAdd = uris.take(availableSlots)
                selectedImageUris.addAll(urisToAdd)
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
                title = { Text(if (isEditMode) "Edit Post" else "Create Forum Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val scope = rememberCoroutineScope()
                    var isUploading by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            scope.launch {
                                isUploading = true
                                try {
                                    val name = getUserName(userId) ?: ""
                                    val photoUrl = getCurrentUserProfileImageUrl() ?: ""

                                    // Separate new images (picked from device) from existing ones (already URLs)
                                    val (newUris, existingUris) = selectedImageUris.partition {
                                        it.scheme == "content" || it.scheme == "file"
                                    }

// Upload only the new ones
                                    val uploadedImages = if (newUris.isNotEmpty()) {
                                        uploadImagesToFirebaseStorage(newUris)
                                    } else {
                                        emptyList()
                                    }

// Combine uploaded new images and kept existing ones
                                    val finalImageUrls = existingUris.map { it.toString() }.toMutableList()
                                    finalImageUrls.addAll(uploadedImages)


// Build the post
                                    val forumPost = forumPostToEdit?.copy(
                                        content = forumContent,
                                        region = currentDistrict.uppercase(),
                                        imageUrls = finalImageUrls,
                                        timestamp = Timestamp.now(),
                                        edited = true
                                    ) ?: ForumPost(
                                        id = UUID.randomUUID().toString(),
                                        content = forumContent,
                                        region = currentDistrict.uppercase(),
                                        authorId = userId,
                                        authorName = name,
                                        authorImageUrl = photoUrl,
                                        imageUrls = finalImageUrls,
                                        timestamp = Timestamp.now(),
                                        edited = false
                                    )

// Save post regardless of image change
                                    onEvent(ForumEvent.SaveForumPost(forumPost))
                                    navController.navigate(Screen.Forum.route)




                                } catch (e: Exception) {
                                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isUploading = false
                                }
                            }
                        },
                        enabled = !isUploading && (forumContent.isNotBlank() || selectedImageUris.isNotEmpty()) && currentDistrict.isNotBlank()
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(if (isEditMode) "Update" else "Post")
                        }
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

                var imageUrl by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    imageUrl = getCurrentUserProfileImageUrl()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "User Profile Image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
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
                                .padding(16.dp),
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
                        fontSize = 24.sp,
                        fontFamily = AlegreyaSansFontFamily,
                        color = Color.Black
                    )
                )
                var selectedImageUriToShow by remember { mutableStateOf<Uri?>(null) }

                // LazyRow to display selected images
                LazyRow(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(selectedImageUris.size) { index ->
                        Box(modifier = Modifier.clickable {
                            selectedImageUriToShow = selectedImageUris[index]
                        }) {
                            AsyncImage(
                                model = selectedImageUris[index],
                                contentDescription = null,
                                modifier = Modifier
                                    .height(150.dp)
                                    .padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { selectedImageUris.removeAt(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White
                                )
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

suspend fun uploadImagesToFirebaseStorage(imageUris: List<Uri>): List<String> {
    val storage = FirebaseStorage.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")

    val uploadedImageUrls = mutableListOf<String>()

    imageUris.forEach { uri ->
        // Check if URI is a valid local file URI
        if (uri.scheme != "content" && uri.scheme != "file") {
            Log.e("Upload", "Invalid URI, skipping: $uri")
            return@forEach
        }

        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("forumImages/$userId/$fileName")

        try {
            // Log before uploading
            Log.d("Upload", "Uploading image: $uri")
            val uploadTask = ref.putFile(uri).await()

            // Log after upload success
            Log.d("Upload", "Image uploaded successfully: $uri")

            // Get the download URL after successful upload
            val downloadUrl = ref.downloadUrl.await().toString()
            uploadedImageUrls.add(downloadUrl)

            // Log the download URL
            Log.d("Upload", "Download URL: $downloadUrl")
        } catch (e: Exception) {
            // Log the error
            Log.e("Upload", "Upload failed for $uri", e)
            throw e // Re-throw the exception to propagate it to the caller
        }
    }

    return uploadedImageUrls
}



suspend fun getCurrentUserProfileImageUrl(): String? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

    return try {
        val document = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .await()

        document.getString("profilePictureUrl") // Adjust the field name if needed
    } catch (e: Exception) {
        null
    }
}

suspend fun getUserName(uid: String): String? {
    val db = FirebaseFirestore.getInstance()
    return try {
        val doc = db.collection("users").document(uid).get().await()
        doc.getString("fullName")
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching user name", e)
        null
    }
}

