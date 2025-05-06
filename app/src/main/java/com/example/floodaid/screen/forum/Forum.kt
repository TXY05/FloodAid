package com.example.floodaid.screen.forum

import BottomBar
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.example.floodaid.R
import com.example.floodaid.composable.ForumTopBar
import com.example.floodaid.models.Screen
import com.example.floodaid.ui.theme.AlegreyaFontFamily
import com.example.floodaid.ui.theme.AlegreyaSansFontFamily
import com.example.floodaid.viewmodel.ForumViewModel
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Forum(
    navController: NavHostController,
    viewModel: ForumViewModel
) {

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        ForumScreen(
            modifier = Modifier.padding(paddingValues), navController,viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ForumViewModel
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    var showCommentSheet by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.state.collectAsState()
    val forumPosts = uiState.forumPosts

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
                    thickness = 3.dp
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
        val (avatar, name, time, area, image, like, comment, likes, content, comments) = createRefs()

        val imageBitmap = remember(forumPost.imageUrls) {
            base64ToImageBitmap(forumPost.imageUrls.firstOrNull() ?: "")
        }

        Base64Image(
            base64String = forumPost.authorImageBase64,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .padding(start = 16.dp)
                .constrainAs(avatar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
        )


        // Avatar
       /* Image(
            painter = painterResource(id = R.drawable.dummy_avatar),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .constrainAs(avatar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                })*/

        // Name
        Text(
            text = forumPost.authorId,
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

        // Image
        Image(
            painter = painterResource(id = R.drawable.user1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .constrainAs(image) {
                    top.linkTo(content.bottom, margin = 16.dp)
                }
        )

        // Animated Heart Icon
        AnimatedHeartToggle(
            initiallyLiked = false,
            onToggle = { liked -> /* Handle like status */ },
            modifier = Modifier.constrainAs(like) {
                top.linkTo(image.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
            }
        )

        // Likes count
        Text(
            text = "1,123",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black,
            modifier = Modifier.constrainAs(likes) {
                top.linkTo(image.bottom, margin = 18.dp)
                start.linkTo(like.end, margin = 8.dp)
            }
        )

        // Comment Button
        IconButton(
            onClick = { onShowCommentSheetChanged(true) },
            modifier = Modifier
                .size(24.dp)
                .constrainAs(comment) {
                    top.linkTo(image.bottom, margin = 16.dp)
                    start.linkTo(likes.end, margin = 16.dp)
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.comment_icon),
                contentDescription = null
            )
        }

        // Comments count
        Text(
            text = "1,000",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black,
            modifier = Modifier.constrainAs(comments) {
                top.linkTo(image.bottom, margin = 18.dp)
                start.linkTo(comment.end, margin = 8.dp)
            }
        )
    }
}

@Composable
fun AnimatedHeartToggle(
    modifier: Modifier = Modifier,
    initiallyLiked: Boolean = false,
    onToggle: (Boolean) -> Unit,
) {
    var isLiked by remember { mutableStateOf(initiallyLiked) }

    val transition = updateTransition(targetState = isLiked, label = "heart_transition")

    // Scale animation
    val scale by transition.animateFloat(
        label = "scale",
        transitionSpec = {
            keyframes {
                durationMillis = 300
                1.2f at 100
                0.9f at 200
                1f at 300
            }
        }
    ) { trueOrFalse -> if (trueOrFalse) 1.2f else 1f }

    // Heart icon and color
    val heartIcon = if (isLiked) R.drawable.heart_filled else R.drawable.heart_icon
    val heartColor = if (isLiked) Color.Red else Color.Gray

    Icon(
        painter = painterResource(id = heartIcon),
        contentDescription = null,
        tint = heartColor,
        modifier = modifier
            .size(24.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable {
                isLiked = !isLiked
                onToggle(isLiked)
            }
    )
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

fun base64ToImageBitmap(base64String: String): ImageBitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            ?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun Base64Image(base64String: String, modifier: Modifier = Modifier) {
    val imageBitmap = remember(base64String) {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    }
}
