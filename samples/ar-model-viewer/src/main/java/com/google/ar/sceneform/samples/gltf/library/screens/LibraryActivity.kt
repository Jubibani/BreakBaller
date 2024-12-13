package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.View
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.Activity
import com.google.ar.sceneform.samples.gltf.library.gallery.AmphibianActivity
import com.google.ar.sceneform.samples.gltf.library.gallery.BacteriaActivity
import com.google.ar.sceneform.samples.gltf.library.gallery.DigestiveActivity
import com.google.ar.sceneform.samples.gltf.library.gallery.HeartActivity
import com.google.ar.sceneform.samples.gltf.library.gallery.PlatypusActivity
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext


class LibraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AugmentEDTheme {
                LibraryScreen(finish = { finish()})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(finish: () -> Unit) {
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
                ModelItem(item)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelItem(item: ModelItemData) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                val activityClass = modelActivityMap[item.name]
                if (activityClass != null) {
                    val intent = Intent(context, activityClass).apply {
                        putExtra("modelPath", item.modelPath)
                    }
                    context.startActivity(intent)
                } else {
                    // Fallback or error handling if the activity is not found
                    Toast.makeText(context, "Activity not found for ${item.name}", Toast.LENGTH_SHORT).show()
                }
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

val modelActivityMap = mapOf(
    "Amphibian" to AmphibianActivity::class.java,
    "Bacteria" to BacteriaActivity::class.java,
    "Digestive System" to DigestiveActivity::class.java,
    "Platypus" to PlatypusActivity::class.java,
    "Heart" to HeartActivity::class.java
    // Add more mappings as needed
)
fun getModelItems(): List<ModelItemData> {
    return listOf(
        ModelItemData("Bacteria", "models/bacteria.glb", R.drawable.bacteria),
        ModelItemData("Amphibian", "models/frog.glb", R.drawable.amphibian),
        ModelItemData("Digestive System", "models/digestive.glb", R.drawable.digestive),
        ModelItemData("Platypus", "models/platypus.glb", R.drawable.platypus),
        ModelItemData("Heart", "models/bacteria.glb", R.drawable.heart),
        // Add more items as needed
    )
}