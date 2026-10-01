package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.EditorTool
import com.example.ui.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorWorkspaceScreen(
    viewModel: VideoEditorViewModel,
    uiState: com.example.ui.EditorUiState,
    onBackToProjects: () -> Unit
) {
    val project = uiState.currentProject

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Video Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.selectTool(EditorTool.EXPORT) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
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
            // Video Preview Area (Center)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayCircleFilled,
                        contentDescription = "Preview",
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Preview: ${project?.resolution ?: "1080p"} • Filter: ${project?.filterType ?: "Normal"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!project?.textOverlay.isNullOrBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = project.textOverlay!!,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Playback Control bar inside preview
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White) }
                            Text("00:00 / 00:30", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = {}) { Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = Color.White) }
                            IconButton(onClick = {}) { Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White) }
                        }
                    }
                }
            }

            // Timeline Track Representation (Bottom)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Timeline Tracks", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Video Track (Duration: ${project?.durationSeconds ?: 30}s)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Tools Navigation Bar
            val tools = listOf(
                EditorTool.TRIM to "Trim",
                EditorTool.CROP to "Crop",
                EditorTool.SPEED to "Speed",
                EditorTool.TEXT to "Text",
                EditorTool.AUDIO to "Audio",
                EditorTool.FILTERS to "Filters",
                EditorTool.AI_TOOLS to "AI Tools",
                EditorTool.EXPORT to "Export"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tools) { (tool, label) ->
                    FilterChip(
                        selected = uiState.selectedTool == tool,
                        onClick = { viewModel.selectTool(tool) },
                        label = { Text(label) }
                    )
                }
            }

            // Tool Property Inspector Panel (Right/Bottom Panel)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (uiState.selectedTool) {
                        EditorTool.TRIM -> {
                            Text("Trim Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Select start and end timestamp for trimming.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(value = 0f, onValueChange = {}, modifier = Modifier.fillMaxWidth())
                        }
                        EditorTool.CROP -> {
                            Text("Crop & Resolution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("16:9", "9:16", "1:1", "4:5").forEach { res ->
                                    Button(onClick = {
                                        viewModel.updateCurrentProject { it.copy(resolution = res) }
                                    }) {
                                        Text(res)
                                    }
                                }
                            }
                        }
                        EditorTool.SPEED -> {
                            Text("Playback Speed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { spd ->
                                    OutlinedButton(onClick = {
                                        viewModel.updateCurrentProject { it.copy(speed = spd) }
                                    }) {
                                        Text("${spd}x")
                                    }
                                }
                            }
                        }
                        EditorTool.TEXT -> {
                            var textInput by remember { mutableStateOf(project?.textOverlay ?: "") }
                            Text("Add Text Overlay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = {
                                    textInput = it
                                    viewModel.updateCurrentProject { proj -> proj.copy(textOverlay = it) }
                                },
                                label = { Text("Overlay Text") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        EditorTool.AUDIO -> {
                            Text("Audio & Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Adjust original audio volume", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = project?.volume ?: 1.0f,
                                onValueChange = { vol ->
                                    viewModel.updateCurrentProject { it.copy(volume = vol) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        EditorTool.FILTERS -> {
                            Text("Filters & Effects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Normal", "Grayscale", "Warm", "Cool").forEach { filt ->
                                    Button(onClick = {
                                        viewModel.updateCurrentProject { it.copy(filterType = filt) }
                                    }) {
                                        Text(filt)
                                    }
                                }
                            }
                        }
                        EditorTool.AI_TOOLS -> {
                            Text("Gemini AI Video Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.runAiFeature("summary", project?.title ?: "Video") }) {
                                    Text("AI Summary")
                                }
                                Button(onClick = { viewModel.runAiFeature("social", project?.title ?: "Video") }) {
                                    Text("Social Media Tags")
                                }
                                Button(onClick = { viewModel.runAiFeature("captions", project?.title ?: "Video") }) {
                                    Text("AI Auto Captions")
                                }
                            }
                            if (uiState.isAiLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            if (uiState.aiResultText.isNotBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("AI Result:", fontWeight = FontWeight.Bold)
                                        Text(uiState.aiResultText, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        EditorTool.EXPORT -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Export Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (uiState.isExporting) {
                                    LinearProgressIndicator(progress = { uiState.exportProgress }, modifier = Modifier.fillMaxWidth())
                                    Text("Exporting video... ${(uiState.exportProgress * 100).toInt()}%")
                                } else if (uiState.exportCompleted) {
                                    Text("Export Successful!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Button(onClick = { viewModel.startExport() }) {
                                        Text("Download MP4")
                                    }
                                } else {
                                    Button(onClick = { viewModel.startExport() }) {
                                        Text("Start Export (1080p MP4)")
                                    }
                                }
                            }
                        }
                        else -> {
                            Text("Select a tool above to begin editing.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
