package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditorUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val currentProject: ProjectEntity? = null,
    val selectedTool: EditorTool = EditorTool.TRIM,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportCompleted: Boolean = false,
    val aiResultText: String = "",
    val isAiLoading: Boolean = false
)

enum class EditorTool {
    MEDIA, TRIM, CROP, SPEED, TEXT, AUDIO, FILTERS, TRANSITIONS, AI_TOOLS, EXPORT
}

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository

    init {
        val dao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(dao)
        seedDefaultProjectIfNeeded()
    }

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                _uiState.update { state ->
                    val updatedCurrent = if (state.currentProject == null && projects.isNotEmpty()) {
                        projects.first()
                    } else if (state.currentProject != null) {
                        projects.find { it.id == state.currentProject.id } ?: state.currentProject
                    } else {
                        null
                    }
                    state.copy(projects = projects, currentProject = updatedCurrent)
                }
            }
        }
    }

    private fun seedDefaultProjectIfNeeded() {
        viewModelScope.launch {
            // Check if any project exists
            // Handled reactively or inserted if empty
        }
    }

    fun createProject(title: String) {
        viewModelScope.launch {
            val newProj = ProjectEntity(title = title, videoUri = "android.resource://com.aistudio.aivideoeditor/raw/sample_video")
            val id = repository.insertProject(newProj)
            val created = repository.getProjectById(id)
            if (created != null) {
                _uiState.update { it.copy(currentProject = created) }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _uiState.update { it.copy(currentProject = project, exportCompleted = false) }
    }

    fun updateCurrentProject(update: (ProjectEntity) -> ProjectEntity) {
        val current = _uiState.value.currentProject ?: return
        val updated = update(current).copy(updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(currentProject = updated) }
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun selectTool(tool: EditorTool) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    // AI Features
    fun runAiFeature(featureType: String, contentPrompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiResultText = "") }
            val prompt = when (featureType) {
                "summary" -> "Provide a concise professional video summary, key points, and a catchy suggested title for a video about: $contentPrompt"
                "social" -> "Generate engaging YouTube title, description, Instagram caption, TikTok caption, and 5 relevant hashtags for: $contentPrompt"
                "captions" -> "Generate professional timestamped subtitle captions for a video about: $contentPrompt"
                else -> contentPrompt
            }
            val result = GeminiAiService.generateText(prompt)
            _uiState.update { it.copy(isAiLoading = false, aiResultText = result) }
        }
    }

    fun startExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = 0f, exportCompleted = false) }
            for (i in 1..10) {
                kotlinx.coroutines.delay(300)
                _uiState.update { it.copy(exportProgress = i * 0.1f) }
            }
            _uiState.update { it.copy(isExporting = false, exportCompleted = true) }
        }
    }

    fun resetExportState() {
        _uiState.update { it.copy(exportCompleted = false, exportProgress = 0f) }
    }
}
