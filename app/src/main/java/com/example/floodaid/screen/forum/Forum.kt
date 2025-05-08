package com.example.floodaid.screen.forum

import BottomBar
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.floodaid.R
import com.example.floodaid.composable.ForumTopBar
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.AlegreyaFontFamily
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.ForumViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Forum(
    navController: NavHostController,
    viewModel: ForumViewModel,
) {

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        ForumScreen(
            modifier = Modifier.padding(paddingValues), navController, viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ForumViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }

    val uiState by viewModel.state.collectAsState()
    val forumPosts = uiState.forumPosts

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredPosts = forumPosts.filter {
        it.authorName.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.region.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAndSaveForumPosts()
    }

    if (showCommentSheet && selectedPostId != null) {
        CommentModalBottomSheet(
            show = true,
            onDismiss = {
                showCommentSheet = false
                selectedPostId = null
            },
            postId = selectedPostId!!,
            viewModel = viewModel
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ForumTopBar(
                scrollBehavior = scrollBehavior,
                searchQuery = searchQuery,
                onSearchQueryChanged = { searchQuery = it }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPosts) { post ->
                SocialMediaPost(
                    forumPost = post,
                    onShowCommentSheetChanged = { show ->
                        selectedPostId = post.id
                        showCommentSheet = show
                    },
                    navController = navController,
                    viewModel = viewModel
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    viewModel.setPostToEdit(null)
                    navController.navigate(Screen.ForumPostEditor.route)
                },
                containerColor = Color.Blue,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Post")
            }
        }
    }
}


@Composable
fun SocialMediaPost(
    forumPost: ForumPost,
    onShowCommentSheetChanged: (Boolean) -> Unit,
    navController: NavHostController,
    viewModel: ForumViewModel,
) {

    val scope = rememberCoroutineScope()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        val (avatar, name, time, area, image, comment, content, comments, moreOptions) = createRefs()
        var selectedImageUrl by remember { mutableStateOf<String?>(null) }
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


        // Avatar
        AsyncImage(
            model = if (forumPost.authorImageUrl.isEmpty()) R.drawable.ic_user else forumPost.authorImageUrl,
            contentDescription = "User Profile Image",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .constrainAs(avatar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                })
        //Name
        Text(
            text = forumPost.authorName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(parent.top, margin = 3.dp)
                start.linkTo(avatar.end, margin = 8.dp)
            }
        )

        // Time
        Text(
            text = formatTimestamp(forumPost.timestamp),
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.constrainAs(time) {
                top.linkTo(name.bottom)
                start.linkTo(avatar.end, margin = 8.dp)
            }
        )


        var expanded by remember { mutableStateOf(false) }

        if (forumPost.authorId == userId) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(moreOptions) {
                        end.linkTo(parent.end)
                        top.linkTo(avatar.top)
                        bottom.linkTo(avatar.bottom)
                    }
            ) {
                IconButton(
                    onClick = {
                        expanded = true
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vertical),
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            viewModel.setPostToEdit(forumPost)
                            navController.navigate(Screen.ForumPostEditor.route)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false

                            scope.launch {
                                try {
                                    viewModel.deleteForumPost(forumPost)
                                    Toast.makeText(
                                        navController.context,
                                        "Post deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(Screen.Forum.route)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        navController.context,
                                        "Failed to delete post: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }


        // Area
        Text(
            text = forumPost.region + ": ",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = AlegreyaFontFamily,
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier.constrainAs(area) {
                top.linkTo(avatar.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        if (forumPost.content.isNotBlank()) {
            // Content
            Text(
                text = forumPost.content,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    color = Color.Black
                ),
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .constrainAs(content) {
                        top.linkTo(area.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(0.dp)
                    .constrainAs(content) {
                        top.linkTo(area.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                    })
        }
        if (forumPost.imageUrls.any { it.isNotBlank() }) {
            // Filter out empty URLs
            val validImageUrls = forumPost.imageUrls.filter { it.isNotBlank() }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .constrainAs(image) {
                        top.linkTo(content.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(validImageUrls) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedImageUrl = imageUrl // This opens the full-screen view
                            }
                    )
                }
            }

            if (selectedImageUrl != null) {
                Dialog(onDismissRequest = { selectedImageUrl = null }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { selectedImageUrl = null }, // Tap to close the dialog
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset {
                                    IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                                }
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale =
                                            (scale * zoom).coerceIn(1f, 3f)  // Limit the zoom range
                                        offset = Offset(offset.x + pan.x, offset.y + pan.y)
                                    }
                                }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(0.dp)
                    .constrainAs(image) {
                        top.linkTo(content.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }


        // Comment Button
        IconButton(
            onClick = {
                viewModel.refreshComments(forumPost.id)
                onShowCommentSheetChanged(true)
            },
            modifier = Modifier
                .size(30.dp)
                .constrainAs(comment) {
                    top.linkTo(image.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.comment_icon),
                contentDescription = null
            )
        }

        // Comments count
        Text(
            text = forumPost.commentsCount.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.constrainAs(comments) {
                top.linkTo(image.bottom, margin = 18.dp)
                start.linkTo(comment.end, margin = 8.dp)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentModalBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    postId: String,
    viewModel: ForumViewModel,
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var commentText by rememberSaveable { mutableStateOf("") }
    val comments by viewModel.comments.collectAsState()

    LaunchedEffect(postId) {
        viewModel.observeComments(postId)
        scope.launch { sheetState.show() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
        ) {
            Text("Comments", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(comments) { comment ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    ) {

                        AsyncImage(
                            model = if (comment.authorImageUrl.isNullOrEmpty()) R.drawable.ic_user else comment.authorImageUrl,
                            contentDescription = "User Profile Image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = formatTimestamp(comment.timestamp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Add a comment...") },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 2
                )
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.postComment(postId, commentText)
                            commentText = ""
                        }
                    }, enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Post comment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@Composable
fun CommentItem(comment: ForumComment) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = comment.authorImageUrl,
                contentDescription = "Author Image",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = comment.authorName, fontWeight = FontWeight.Bold)
                Text(
                    text = formatTimestamp(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 44.dp, top = 4.dp)
        )
    }
}


fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp.seconds * 1000)) // Convert to milliseconds
}

