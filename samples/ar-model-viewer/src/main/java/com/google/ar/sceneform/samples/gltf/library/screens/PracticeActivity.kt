package com.google.ar.sceneform.samples.gltf.library.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
                },
                actions = {
                    PointsDisplay(points) // Add PointsDisplay here!
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
    val onClickAction: (PracticeItemData) -> Unit
)

data class RewardItemData(
    val name: String,
    val description: String,
    val imageResId: Int,
    val cost: Int,
    val onClickAction: (RewardItemData) -> Unit
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
                onRedeem(-10) //  Use onRedeem(-10) to add points

                /*  [Needs More Investigation]
                Huh? what happened? add points but 10 below zero?
                 */
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
        LazyColumn {
            items(learnItems) { item ->
                PracticeItemCard(item)
            }
        }
    }
}

@Composable
fun PointsDisplay(points: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp) //outside
            .clip(RoundedCornerShape(10.dp)) // Rounded border
            .background(Color(0xFF2B2B2B)) // Darker background
            .padding(horizontal = 3.dp, vertical = 2.dp) // Padding inside the box

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.brain), // Your brain icon
                contentDescription = "Brain Currency",
                modifier = Modifier.size(24.dp) // Adjust icon size
            )

            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

            Text(
                text = points.toString(), // Show points number
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (points >= 50) Color(0xFFFFD700) else Color.White // Gold for enough points
            )
        }
    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RewardsContent(points: Int, onRedeem: (Int) -> Unit, playSwitchSound: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("rewards_prefs", Context.MODE_PRIVATE)

    val pointsKey = "User_points"
    var userPoints by rememberSaveable { mutableIntStateOf(sharedPreferences.getInt(pointsKey, 0)) }

    var showDialog by remember {mutableStateOf(false)}
    var selectedItem by remember {mutableStateOf<RewardItemData?>(null)}

    val spendPoints: (Int) -> Unit = { amount ->
        userPoints -= amount
        sharedPreferences.edit().putInt(pointsKey, userPoints).apply()
    }

    val rewardItems = remember {
        listOf(
            RewardItemData("1", "Redeem your points", R.drawable.question_icon, 10) {
                playSwitchSound()

                selectedItem = it
                showDialog = true
            },
            RewardItemData("2", "Redeem your points", R.drawable.question_icon, 25) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
            },
            RewardItemData("3", "Redeem your points", R.drawable.question_icon, 10) {
                playSwitchSound()

                selectedItem = it
                showDialog = true
            },
            RewardItemData("4", "Redeem your points", R.drawable.question_icon, 25) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
            },
            RewardItemData("5", "Redeem your points", R.drawable.question_icon, 10) {
                playSwitchSound()

                selectedItem = it
                showDialog = true
            },
            RewardItemData("6", "Redeem your points", R.drawable.question_icon, 25) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
            },
            RewardItemData("7", "Redeem your points", R.drawable.question_icon, 10) {
                playSwitchSound()

                selectedItem = it
                showDialog = true
            },
            RewardItemData("8", "Redeem your points", R.drawable.question_icon, 25) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
            },
            RewardItemData("9", "Redeem your points", R.drawable.question_icon, 10) {
                playSwitchSound()

                selectedItem = it
                showDialog = true
            },
            RewardItemData("10", "Redeem your points", R.drawable.question_icon, 25) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
            },
            RewardItemData("11", "Redeem your points", R.drawable.question_icon, 50) {
                playSwitchSound()
                selectedItem = it
                showDialog = true
                if (userPoints >= 50) {
                    spendPoints(50)
                    onRedeem(50)
                    Toast.makeText(context, "Mini-game unlocked!", Toast.LENGTH_SHORT).show()
                    val activity = context as? Activity
                    activity?.let {
                        val intent = Intent(it, com.unity3d.player.UnityPlayerGameActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                        it.startActivity(intent)
                    } ?: Toast.makeText(context, "Error: Unable to launch Unity", Toast.LENGTH_SHORT).show()
                }
            }

        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columns
        modifier = Modifier.fillMaxSize().padding(8.dp), // Full size + padding
        verticalArrangement = Arrangement.spacedBy(12.dp), // Space between rows
        horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between columns
    ) {
        items(rewardItems) { item ->
            RewardItemCard(item) { selected ->
                selectedItem = selected  //  Store the selected item
                showDialog = true
            }
        }
    }

    if (showDialog && selectedItem != null) {
        ConfirmPurchaseDialog(
            itemName = selectedItem!!.name,
            requiredPoints = selectedItem!!.cost,
            userPoints = userPoints,
            onConfirm = {
                //  Only deduct points AFTER user confirms
                if (userPoints >= selectedItem!!.cost) {
                    spendPoints(selectedItem!!.cost)
                    onRedeem(selectedItem!!.cost)

                    //  Launch additional content if applicable
                    if (selectedItem!!.name == "11") {
                        val activity = context as? Activity
                        activity?.let {
                            val intent = Intent(it, com.unity3d.player.UnityPlayerGameActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                            it.startActivity(intent)
                        }
                    }
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }




}


@Composable
fun PracticeItemCard(item: PracticeItemData) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { item.onClickAction(item) },
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

@Composable
fun RewardItemCard(item: RewardItemData, onItemSelected: (RewardItemData) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onItemSelected(item) }, // ✅ Only select the item, do NOT buy yet
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = item.imageResId), contentDescription = null)
            Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "${item.cost} Points", fontSize = 14.sp, color = Color.Gray)
        }
    }
}


@Composable
fun ConfirmPurchaseDialog(
    itemName: String,
    requiredPoints: Int,
    userPoints: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (userPoints >= requiredPoints) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Confirm Purchase") },
            text = { Text("Do you want to unlock $itemName for $requiredPoints brains?") },
            confirmButton = {
                Button(onClick = { onConfirm() }) {
                    Text("Buy")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Not Enough Brains") },
            text = { Text("You need $requiredPoints brains to unlock $itemName.") },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("OK")
                }
            }
        )
    }
}
