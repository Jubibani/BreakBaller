package com.google.ar.sceneform.samples.gltf.library.screens

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.repository.PointsRepository
import com.google.ar.sceneform.samples.gltf.library.data.viewmodel.RewardsViewModel
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PracticeActivity : FragmentActivity() {
    //Initialize points from db
    private lateinit var repository: PointsRepository

    private var purchaseSound: MediaPlayer? = null
    private var backSound: MediaPlayer? = null
    private var switchSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSounds()

        val rewardsViewModel: RewardsViewModel by viewModels()

        val database = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
        repository = PointsRepository(database.brainPointsDao())

        setContent {
            AugmentEDTheme {
                PracticeScreen(
                    finish = { finish() },
                    repository = repository,
                    playPurchaseSound = {playPurchaseSound() },
                    playBackSound = { playBackSound() },
                    playSwitchSound = { playSwitchSound() },
                    rewardsViewModel = rewardsViewModel
                )
            }
        }
    }


    private fun initializeSounds() {
        try {
            purchaseSound = MediaPlayer.create(this, R.raw.purchase)
            backSound = MediaPlayer.create(this, R.raw.back)
            switchSound = MediaPlayer.create(this, R.raw.swipe)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playPurchaseSound() {
        purchaseSound?.let { sound ->
            if (!sound.isPlaying) {
                sound.start()
            }
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
        purchaseSound?.apply {
            if (isPlaying) stop()
            release()
        }


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
    repository: PointsRepository,
    playPurchaseSound: () -> Unit,
    playBackSound: () -> Unit,
    playSwitchSound: () -> Unit,
    rewardsViewModel: RewardsViewModel
) {
    val scope = rememberCoroutineScope()
    val pointsFlow = repository.pointsFlow.collectAsState(initial = null)
    val points = pointsFlow.value?.points ?: 0 //  Get current points from Flow


    PointsDisplay(points)


    val addPoints: (Int) -> Unit = { earnedPoints ->
        scope.launch {
            val currentPoints = repository.getPoints() // Get current points
            val newPoints = currentPoints + earnedPoints
            repository.updatePoints(newPoints) // Update points

            Log.d("PointsDebug", "Updated Points: $newPoints") //  Debugging log
        }
    }


    Column {
        PointsDisplay(points = points) //  Show updated points
        Button(onClick = { addPoints(10) }) { // Example button to add points
            Text("Earn 10 Points")
        }
    }

    val onRedeem: (Int) -> Unit = { cost ->
        if (points >= cost) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updatePoints(points - cost) //  Deduct points
            }
        }
    }

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
                        finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back to Main"
                        )
                    }
                },
                actions = {
                    PointsDisplay(points)
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
                listOf("Learn and Earn", "Rewards").forEachIndexed { index, title ->
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
                0 -> LearnAndEarnContent(playSwitchSound, addPoints, onRedeem) // ✅ Pass addPoints
                1 -> RewardsContent(points, onRedeem, playSwitchSound, playPurchaseSound, rewardsViewModel)
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
    val id: String,
    val name: String,
    val description: String,
    val imageResId: Int,
    val cost: Int,
    val isUnlocked: Boolean,
    val onClickAction: (RewardItemData) -> Unit
)


@Composable
fun LearnAndEarnContent(
    playSwitchSound: () -> Unit,
    addPoints: (Int) -> Unit,
    onRedeem: (Int) -> Unit
) {
    val context = LocalContext.current


    val learnItems = remember {
        listOf(
            PracticeItemData("Quiz Challenge", "Test your knowledge", R.drawable.quiz_icon) {
                playSwitchSound()

                addPoints(10)
            },
            PracticeItemData("Flashcards", "Review key concepts", R.drawable.flashcard_icon) {
                playSwitchSound()

            },
            PracticeItemData("AR Practice", "Learn with augmented reality", R.drawable.quiz_icon) {
                playSwitchSound()

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

@Composable
fun RewardsContent(
    points: Int,
    onRedeem: (Int) -> Unit,
    playSwitchSound: () -> Unit,
    playPurchaseSound: () -> Unit,
    viewModel: RewardsViewModel
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<RewardItemData?>(null) }



    //  Automatically updates UI when StateFlow changes
    val rewardItems by viewModel.rewardItems.collectAsState()

    Log.d("UI Debug", "Reward items updated: $rewardItems") // 🔥 DEBUG HERE

    if (points > 0) {
        // Show rewards if the user has points
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rewardItems) { item ->
                RewardItemCard(item) { selected ->
                    Log.d("MiniGameDebug", "Item clicked: ${selected.name}, isUnlocked=${selected.isUnlocked}")
                    if (!item.isUnlocked) {
                        selectedItem = selected
                        showDialog = true
                    } else {
                        selected.onClickAction.invoke(selected) // Invoke the function
                    }
                }

            }
        }
    } else {
        // Show message when the user has no points
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.brain), // Use your brain icon here
                contentDescription = "Brain points icon",
                modifier = Modifier.size(80.dp), // Adjust size as needed
                tint = Color.Unspecified // Keeps original icon color
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Collect Brain Points!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        }

    }

    // Show purchase confirmation dialog
    if (showDialog && selectedItem != null) {
        ConfirmPurchaseDialog(
            itemName = selectedItem!!.name,
            requiredPoints = selectedItem!!.cost,
            userPoints = points,
            isUnlocked = selectedItem!!.isUnlocked,
            onConfirm = {
                playPurchaseSound()
                showDialog = false
                selectedItem?.let { selected ->
                    viewModel.unlockMiniGameAndDeductPoints(selected.id, selected.cost)
                    Toast.makeText(context, "Mini-game unlocked!", Toast.LENGTH_SHORT).show()
                }
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
fun RewardItemCard(item: RewardItemData, onClick: (RewardItemData) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(100.dp)
            .clickable { onClick(item) }
            .border(
                width = 2.dp,
                color = if (item.isUnlocked) Color.Green else Color.Gray, //  Green if unlocked, Gray if locked
                shape = RoundedCornerShape(10.dp)
            )
            .alpha(if (item.isUnlocked) 1f else 0.5f), // ✅ Dim locked items
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (item.isUnlocked) Icons.Default.VideogameAsset else Icons.Default.Lock, // 🎮 or  icon
                    contentDescription = null,
                    tint = if (item.isUnlocked) Color.Green else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.name, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}



@Composable
fun ConfirmPurchaseDialog(
    itemName: String,
    requiredPoints: Int,
    userPoints: Int,
    isUnlocked: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // Snackbar State

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Unlock $itemName?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Cost: $requiredPoints Brain Points", fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (userPoints >= requiredPoints) {
                                onConfirm()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Mini-game unlocked!") //  Success Message
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Not enough points!") //  Error Message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userPoints >= requiredPoints) Color.Green else Color.Gray
                        ),
                        enabled = userPoints >= requiredPoints // Disable if not enough points
                    ) {
                        Text("Unlock")
                    }
                }
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState) // Show Snackbar at the bottom
}