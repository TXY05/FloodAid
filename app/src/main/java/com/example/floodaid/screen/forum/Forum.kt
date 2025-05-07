package com.example.floodaid.screen.forum

import BottomBar
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    val uiState by viewModel.state.collectAsState()
    val forumPosts = uiState.forumPosts




    LaunchedEffect(Unit) {
        viewModel.fetchAndSaveForumPosts()
    }

    CommentModalBottomSheet(
        show = showCommentSheet,
        onDismiss = { showCommentSheet = false }
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ForumTopBar(scrollBehavior = scrollBehavior)
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            items(forumPosts) { post ->

                SocialMediaPost(
                    forumPost = post,
                    onShowCommentSheetChanged = { showCommentSheet = it }
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
                    navController.navigate(Screen.CreateForumPost.route)
                },
                containerColor = Color.Blue,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "")
            }
        }
    }
}


@Composable
fun SocialMediaPost(
    forumPost: ForumPost,
    onShowCommentSheetChanged: (Boolean) -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        val (avatar, name, time, area, image, comment, content, comments) = createRefs()
        var selectedImageUrl by remember { mutableStateOf<String?>(null) }
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }


        // Avatar
        AsyncImage(
            model = forumPost.authorImageUrl,
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
                items(forumPost.imageUrls) { imageUrl ->
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
                                            (scale * zoom).coerceIn(
                                                1f,
                                                3f
                                            )  // Limit the zoom range
                                        offset = Offset(
                                            offset.x + pan.x,
                                            offset.y + pan.y
                                        )
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
            onClick = { onShowCommentSheetChanged(true) },
            modifier = Modifier
                .size(24.dp)
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
            style = MaterialTheme.typography.titleSmall,
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
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            sheetState.show()
        }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Comments", style = MaterialTheme.typography.titleLarge)
            // Add more comment content here
        }
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp.seconds * 1000)) // Convert to milliseconds
}
