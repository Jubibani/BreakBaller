package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme



class PracticeActivity : FragmentActivity() {
    private var backSound: MediaPlayer? = null
    private var switchSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSounds()
        setContent {
            AugmentEDTheme {
                PracticeScreen(
                    finish = { finish() },
                    playBackSound = { playBackSound() },
                    playSwitchSound = { playSwitchSound() }
                )
            }
        }
    }


    private fun initializeSounds() {
        try {
            backSound = MediaPlayer.create(this, R.raw.back)
            switchSound = MediaPlayer.create(this, R.raw.swipe)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playBackSound() {
        backSound?.let { sound ->
            if (!sound.isPlaying) {
                sound.start()
            }
        }
    }

    private fun playSwitchSound() {
        switchSound?.let { sound ->
            if (!sound.isPlaying) {
                sound.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayers()
    }

    private fun releaseMediaPlayers() {
        backSound?.apply {
            if (isPlaying) stop()
            release()
        }
        switchSound?.apply {
            if (isPlaying) stop()
            release()
        }
        backSound = null
        switchSound = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    finish: () -> Unit,
    playBackSound: () -> Unit,
    playSwitchSound: () -> Unit
) {
    val context = LocalContext.current
    val tabTitles = remember { listOf("Learn and Earn", "Rewards") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        playBackSound()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to Main"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            var selectedTabIndex by remember { mutableStateOf(0) }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFFD700) // Gold color for the indicator
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            playSwitchSound()
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color(0xFFFFD700) else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> LearnAndEarnContent(playSwitchSound)
                1 -> RewardsContent(playSwitchSound)
            }
        }
    }
}
data class PracticeItemData(
    val name: String,
    val description: String,
    val imageResId: Int,
    val onClickAction: () -> Unit
)
@Composable
fun LearnAndEarnContent(playSwitchSound: () -> Unit) {
    val context = LocalContext.current
    val learnItems = remember {
        listOf(
            PracticeItemData("Quiz Challenge", "Test your knowledge", R.drawable.quiz_icon) {
                playSwitchSound()
                Toast.makeText(context, "Starting Quiz Challenge", Toast.LENGTH_SHORT).show()
                // Add intent to start Quiz activity
            },
            PracticeItemData("Flashcards", "Review key concepts", R.drawable.flashcard_icon) {
                playSwitchSound()
                Toast.makeText(context, "Opening Flashcards", Toast.LENGTH_SHORT).show()
                // Add intent to start Flashcards activity
            },
            PracticeItemData("AR Practice", "Learn with augmented reality", R.drawable.quiz_icon) {
                playSwitchSound()
                Toast.makeText(context, "Launching AR Practice", Toast.LENGTH_SHORT).show()
                // Add intent to start AR Practice activity
            }
        )
    }

    LazyColumn {
        items(learnItems) { item ->
            PracticeItemCard(item)
        }
    }
}

@Composable
fun RewardsContent(playSwitchSound: () -> Unit) {
    val context = LocalContext.current
    val rewardItems = remember {
        listOf(
            PracticeItemData("Achievements", "View your accomplishments", R.drawable.question_icon) {
                playSwitchSound()
                Toast.makeText(context, "Viewing Achievements", Toast.LENGTH_SHORT).show()
                // Add intent to start Achievements activity
            },
            PracticeItemData("Leaderboard", "See how you rank", R.drawable.question_icon) {
                playSwitchSound()
                Toast.makeText(context, "Opening Leaderboard", Toast.LENGTH_SHORT).show()
                // Add intent to start Leaderboard activity
            },
            PracticeItemData("Redeem Points", "Use your earned points", R.drawable.question_icon) {
                playSwitchSound()
                Toast.makeText(context, "Redeeming Points", Toast.LENGTH_SHORT).show()
                // Add intent to start Redeem Points activity
            }
        )
    }

    LazyColumn {
        items(rewardItems) { item ->
            PracticeItemCard(item)
        }
    }
}

@Composable
fun PracticeItemCard(item: PracticeItemData) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = item.onClickAction),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = "Icon for ${item.name}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}