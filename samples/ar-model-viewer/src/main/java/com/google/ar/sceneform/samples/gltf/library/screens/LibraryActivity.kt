package com.google.ar.sceneform.samples.gltf.library.screens

import LibraryFragment
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View.GONE
import android.widget.ImageButton
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme


class LibraryActivity : FragmentActivity() {

    private lateinit var backSound: MediaPlayer
    private lateinit var flipSound: MediaPlayer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backSound = MediaPlayer.create(this, R.raw.back)
        flipSound = MediaPlayer.create(this, R.raw.flip)

        setContent {

            AugmentEDTheme {

                LibraryScreen(
                    finish = { finish() },
                    playBackSound = { playBackSound() },
                    playFlipSound = { playFlipSound() },
                    onModelSelected = { modelData ->
                        val libraryFragment = LibraryFragment().apply {
                            arguments = Bundle().apply {
                                putString("modelName", modelData.name) // modelData is now correctly defined
                            }
                        }

                        supportFragmentManager.beginTransaction()
                            .replace(android.R.id.content, libraryFragment)
                            .addToBackStack(null)
                            .commit()
                    }

                )
            }
        }
    }
    private fun playBackSound() {
        backSound.start()
    }

    private fun playFlipSound() {
        flipSound.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        backSound.release()
        flipSound.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    finish: () -> Unit,
    playBackSound: () -> Unit,
    playFlipSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library", style = MaterialTheme.typography.headlineMedium) },
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
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(getModelItems()) { item ->
                ModelItem(item, playFlipSound, onModelSelected)
            }
        }
    }
}

@Composable
fun ModelItem(
    item: ModelItemData,
    playFlipSound: () -> Unit,
    onModelSelected: (ModelItemData) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                playFlipSound()
                onModelSelected(item)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Image(
                painter = painterResource(id = item.previewImageResId),
                contentDescription = "Preview of ${item.name}",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class ModelItemData(
    val name: String,
    val modelPath: String,
    val previewImageResId: Int
)

/* [Old Code]
val modelActivityMap = mapOf(
    "Amphibian" to AmphibianActivity::class.java,
    "Bacteria" to BacteriaActivity::class.java,
    "Digestive System" to DigestiveActivity::class.java,
    "Platypus" to PlatypusActivity::class.java,
    "Heart" to HeartActivity::class.java
    // Add more mappings as needed
)*/

fun getModelItems(): List<ModelItemData> {
    return listOf(
        //{FIRST BATCH OF 1-9 PAGES}
        //page 223 (num_1)
        ModelItemData("Bacteria", "models/bacteria.glb", R.drawable.bacteria),
        ModelItemData("Amphibian", "models/amphibian.glb", R.drawable.amphibian),
        ModelItemData("Digestive", "models/digestive.glb", R.drawable.digestive),
        ModelItemData("Platypus", "models/platypus.glb", R.drawable.platypus),
        ModelItemData("Heart", "models/heart.glb", R.drawable.heart),
        ModelItemData("Microscope", "models/microscope.glb", R.drawable.microscrope),
        ModelItemData("Earth", "models/earth.glb", R.drawable.earth),
        ModelItemData("Animal", "models/animal.glb", R.drawable.animals),
        ModelItemData("Plant", "models/plant.glb", R.drawable.plant),
        ModelItemData("Biotic", "models/biotic.glb", R.drawable.biotic),
        ModelItemData("Cell", "models/cell.glb", R.drawable.cell),
        ModelItemData("Biodiversity", "models/biodiversity.glb", R.drawable.biodiversity),
        ModelItemData("Organism", "models/organism.glb", R.drawable.organism),
        ModelItemData("Ecosystem", "models/ecosystem.glb", R.drawable.ecosystem),
        ModelItemData("Agriculture", "models/agriculture.glb", R.drawable.agriculture),
        ModelItemData("Medicine", "models/medicine.glb", R.drawable.medicine),

        //page 224 (num_2)
        ModelItemData("Reptile", "models/reptile.glb", R.drawable.reptile),
        ModelItemData("Forest", "models/forest.glb", R.drawable.forest),
        ModelItemData("Ocean", "models/ocean.glb", R.drawable.ocean),

        ModelItemData("River", "models/river.glb", R.drawable.river),

        ModelItemData("Soil", "models/soil.glb", R.drawable.soil),
        ModelItemData("Human", "models/human.glb", R.drawable.human),
        ModelItemData("Acacia", "models/acacia.glb", R.drawable.acacia),
        ModelItemData("Bacterium", "models/bacteria.glb", R.drawable.bacteria),
        ModelItemData("Biological", "models/biological.glb", R.drawable.biological),
        ModelItemData("Specie", "models/specie.glb", R.drawable.specie),

        //page 225 (num_3)
            // [organism] (already implemented)

        //page 226 (num_4)
        ModelItemData("Scientist", "models/scientist.glb", R.drawable.scientist),


        // Add more items as needed
    )
}