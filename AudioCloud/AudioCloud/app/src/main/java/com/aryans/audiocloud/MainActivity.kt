package com.aryans.audiocloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // ktlint-disable no-wildcard-imports
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.util.concurrent.TimeUnit

data class Audiobook(
    val title: String,
    val author: String,
    @DrawableRes val coverArtId: Int,
    @RawRes val audioResId: Int,
    val summary: String
)

val audiobooks = listOf(
    Audiobook(
        title = "The Adventures of Sherlock Holmes",
        author = "Arthur Conan Doyle",
        coverArtId = R.drawable.sherlock_holmes_cover,
        audioResId = R.raw.holmes_01_doyle,
        summary = "A collection of twelve short stories featuring the consulting detective Sherlock Holmes."
    ),
    Audiobook(
        title = "Pride and Prejudice",
        author = "Jane Austen",
        coverArtId = R.drawable.pride_and_prejudice_cover,
        audioResId = R.raw.pride_and_prejudice_01,
        summary = "A romantic novel that charts the emotional development of the protagonist Elizabeth Bennet."
    ),
    Audiobook(
        title = "Frankenstein",
        author = "Mary Shelley",
        coverArtId = R.drawable.frankenstein_cover,
        audioResId = R.raw.frankenstein_01,
        summary = "The story of Victor Frankenstein, a young scientist who creates a sapient creature in an unorthodox scientific experiment."
    ),
    Audiobook(
        title = "The Time Machine",
        author = "H.G. Wells",
        coverArtId = R.drawable.time_machine_cover,
        audioResId = R.raw.time_machine_01,
        summary = "A science fiction novella about a time traveller who witnesses the future of humanity."
    ),
    Audiobook(
        title = "Dracula",
        author = "Bram Stoker",
        coverArtId = R.drawable.dracula_cover,
        audioResId = R.raw.dracula_01,
        summary = "An epistolary novel about the vampire Dracula's attempt to move from Transylvania to England."
    )
)

class MainActivity : ComponentActivity() {

    private val audiobookViewModel by viewModels<AudiobookViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "playlist") {
                    composable("playlist") {
                        PlaylistScreen(navController = navController, viewModel = audiobookViewModel)
                    }
                    composable(
                        "player/{bookIndex}",
                        arguments = listOf(navArgument("bookIndex") { type = NavType.IntType }),
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700)) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700)) }
                    ) { backStackEntry ->
                        val bookIndex = backStackEntry.arguments?.getInt("bookIndex") ?: 0
                        AudiobookPlayerScreen(
                            navController = navController,
                            audiobook = audiobooks[bookIndex],
                            viewModel = audiobookViewModel
                        )
                    }
                    composable(
                        "details/{bookIndex}",
                        arguments = listOf(navArgument("bookIndex") { type = NavType.IntType }),
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700)) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700)) }
                    ) { backStackEntry ->
                        val bookIndex = backStackEntry.arguments?.getInt("bookIndex") ?: 0
                        BookDetailsScreen(navController = navController, audiobook = audiobooks[bookIndex])
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController, viewModel: AudiobookViewModel) {
    val currentBook by viewModel.currentBook.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AudioCloud", fontFamily = FontFamily.Serif) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color.White)
                    )
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = currentBook != null) {
                MiniPlayer(navController = navController, viewModel = viewModel)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            itemsIndexed(audiobooks) { index, audiobook ->
                val isCurrentlyPlaying = isPlaying && currentBook == audiobook
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { 
                            viewModel.play(audiobook)
                            navController.navigate("player/$index") 
                        },
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = audiobook.coverArtId),
                            contentDescription = "Book Cover",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = audiobook.title, fontWeight = FontWeight.Bold)
                            Text(text = audiobook.author, color = Color.Gray)
                        }
                        if (isCurrentlyPlaying) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Now Playing")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(navController: NavController, viewModel: AudiobookViewModel) {
    val currentBook by viewModel.currentBook.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    currentBook?.let { book ->
        val bookIndex = audiobooks.indexOf(book)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("player/$bookIndex") },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = book.coverArtId),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = book.title, fontWeight = FontWeight.Bold)
                    Text(text = book.author, fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = { if (isPlaying) viewModel.pause() else viewModel.resume() }) {
                    Icon(
                        painter = painterResource(id = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookPlayerScreen(navController: NavController, audiobook: Audiobook, viewModel: AudiobookViewModel) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()

    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    val formattedTotalDuration = formatDuration(totalDuration)
    val formattedCurrentTime = formatDuration(currentPosition)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(audiobook.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Image(
                        painter = painterResource(id = audiobook.coverArtId),
                        contentDescription = "Book Cover",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .rotate(if (isPlaying) rotationAngle else 0f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = audiobook.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = audiobook.author,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val bookIndex = audiobooks.indexOf(audiobook)
                        Button(onClick = { navController.navigate("details/$bookIndex") }) {
                            Text("View Details")
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                            onValueChange = { newProgress -> viewModel.seekTo(newProgress) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formattedCurrentTime, fontSize = 12.sp, color = Color.Gray)
                            Text(text = formattedTotalDuration, fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { 
                                if (isPlaying) viewModel.pause() else viewModel.play(audiobook)
                            },
                            shape = RoundedCornerShape(50), // Circular
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                                ),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.fillMaxSize(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(navController: NavController, audiobook: Audiobook) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(audiobook.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Card(
            modifier = Modifier.padding(padding)
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = audiobook.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = audiobook.summary,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

fun formatDuration(duration: Int): String {
    if (duration < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
