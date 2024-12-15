package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.adapters.PracticeViewPagerAdapter
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
            AndroidView(
                factory = { context ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )

                        val tabLayoutView = TabLayout(context).apply {
                            tabMode = TabLayout.MODE_FIXED
                            tabGravity = TabLayout.GRAVITY_FILL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setSelectedTabIndicatorColor(context.getColor(R.color.gold))
                            setTabTextColors(
                                context.getColor(R.color.off_white),
                                context.getColor(R.color.gold)
                            )
                        }
                        addView(tabLayoutView)

                        val viewPagerView = ViewPager2(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            ).apply {
                                weight = 1f
                            }
                            adapter = PracticeViewPagerAdapter(context as FragmentActivity)
                        }
                        addView(viewPagerView)

                        TabLayoutMediator(tabLayoutView, viewPagerView) { tab, position ->
                            tab.text = tabTitles[position]
                        }.attach()

                        // Set up listeners for both TabLayout and ViewPager2
                        tabLayoutView.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab?) {
                                playSwitchSound()
                            }
                            override fun onTabUnselected(tab: TabLayout.Tab?) {}
                            override fun onTabReselected(tab: TabLayout.Tab?) {}
                        })

                        viewPagerView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                playSwitchSound()
                            }
                        })

                        this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}