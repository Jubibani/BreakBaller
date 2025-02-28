package com.google.ar.sceneform.samples.gltf.library.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val sharedPreferences = context.getSharedPreferences("rewards_prefs", Context.MODE_PRIVATE)

    val pointsKey = "User_points"
    var points by rememberSaveable { mutableIntStateOf(sharedPreferences.getInt(pointsKey, 0)) }

    val onRedeem: (Int) -> Unit = { cost ->
        if (points >= cost) {
            points -= cost
            sharedPreferences.edit().putInt(pointsKey, points).apply()
        }
    }

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
                        finish() // Instead of restarting MainActivity
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
                0 -> LearnAndEarnContent(playSwitchSound, points, onRedeem) //  Pass points & onRedeem
                1 -> RewardsContent(points, onRedeem, playSwitchSound) //  Pass points & onRedeem
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
fun LearnAndEarnContent(
    playSwitchSound: () -> Unit,
    points: Int,
    onRedeem: (Int) -> Unit
) {
    val context = LocalContext.current

    val learnItems = remember {
        listOf(
            PracticeItemData("Quiz Challenge", "Test your knowledge", R.drawable.quiz_icon) {
                playSwitchSound()
                Toast.makeText(context, "Starting Quiz Challenge", Toast.LENGTH_SHORT).show()
                onRedeem(-10) // ✅ Use onRedeem(-10) to add points
            },
            PracticeItemData("Flashcards", "Review key concepts", R.drawable.flashcard_icon) {
                playSwitchSound()
                Toast.makeText(context, "Opening Flashcards", Toast.LENGTH_SHORT).show()
            },
            PracticeItemData("AR Practice", "Learn with augmented reality", R.drawable.quiz_icon) {
                playSwitchSound()
                Toast.makeText(context, "Launching AR Practice", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        PointsDisplay(points)
        LazyColumn {
            items(learnItems) { item ->
                PracticeItemCard(item)
            }
        }
    }
}

@Composable
fun PointsDisplay(points: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Points: $points",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (points >= 50) Color(0xFFFFD700) else Color.Red // Highlight when enough points
        )
        Icon(imageVector = Icons.Default.Star, contentDescription = "Points Icon", tint = Color(0xFFFFD700))
    }
}

@Composable
fun RedeemButton(points: Int, onRedeem: (Int) -> Unit) {
    val context = LocalContext.current

    Button(
        onClick = {
            if (points >= 50) {
                onRedeem(50) // Deduct 50 points to unlock content
                Toast.makeText(context, "Mini-game unlocked!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Not enough points!", Toast.LENGTH_SHORT).show()
            }
        },
        enabled = points >= 50
    ) {
        Text("Unlock Mini-Game (50 points)")
    }
}


@Composable
fun RewardsContent(points: Int, onRedeem: (Int) -> Unit, playSwitchSound: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("rewards_prefs", Context.MODE_PRIVATE)

    val pointsKey = "User_points"
    var userPoints by rememberSaveable { mutableIntStateOf(sharedPreferences.getInt(pointsKey, 0)) }

    val spendPoints: (Int) -> Unit = { amount ->
        userPoints -= amount
        sharedPreferences.edit().putInt(pointsKey, userPoints).apply()
    }

    val rewardItems = remember {
        listOf(
            PracticeItemData("Additional Content 1", "Redeem your points", R.drawable.question_icon) {
                playSwitchSound()
                Toast.makeText(context, "Viewing Achievements", Toast.LENGTH_SHORT).show()
            },
            PracticeItemData("Additional Content 2", "Redeem your points", R.drawable.question_icon) {
                playSwitchSound()
                Toast.makeText(context, "Viewing Achievements", Toast.LENGTH_SHORT).show()
            },
            PracticeItemData("Additional Content 3", "Redeem your points", R.drawable.question_icon) {
                playSwitchSound()

                if (userPoints >= 50) {
                    spendPoints(50) // Deduct 50 points to unlock content
                    onRedeem(50)
                    Toast.makeText(context, "Mini-game unlocked!", Toast.LENGTH_SHORT).show()

                    val activity = context as? Activity
                    if (activity != null) {
                        val intent = Intent(activity, com.unity3d.player.UnityPlayerGameActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT // Ensures result returns to this Activity
                        activity.startActivity(intent) // Launch Unity
                    } else {
                        Toast.makeText(context, "Error: Unable to launch Unity", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Not enough points!", Toast.LENGTH_SHORT).show()
                }
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