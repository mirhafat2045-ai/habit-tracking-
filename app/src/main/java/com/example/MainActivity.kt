package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VideoEditorViewModel
import com.example.ui.screens.EditorWorkspaceScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AiVideoEditorApp()
            }
        }
    }
}

@Composable
fun AiVideoEditorApp() {
    val viewModel: VideoEditorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("projects") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentScreen == "projects") {
            ProjectsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onOpenEditor = { currentScreen = "editor" }
            )
        } else {
            EditorWorkspaceScreen(
                viewModel = viewModel,
                uiState = uiState,
                onBackToProjects = { currentScreen = "projects" }
            )
        }
    }
}
