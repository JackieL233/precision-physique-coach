package com.iwanttobeanifbbpro.app.ui

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iwanttobeanifbbpro.app.core.CoachMode
import com.iwanttobeanifbbpro.app.core.SkillAssetRepository
import com.iwanttobeanifbbpro.app.core.SkillPromptBuilder
import com.iwanttobeanifbbpro.app.network.OpenAiResponsesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4.1-mini"
)

data class ImageAttachment(
    val name: String,
    val mimeType: String,
    val base64: String
)

data class CoachUiState(
    val mode: CoachMode = CoachMode.LINKED_TRAINING_NUTRITION,
    val userInput: String = CoachMode.LINKED_TRAINING_NUTRITION.defaultPrompt,
    val settings: AiSettings = AiSettings(),
    val images: List<ImageAttachment> = emptyList(),
    val result: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CoachViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)
    private val promptBuilder = SkillPromptBuilder(SkillAssetRepository(application))
    private val apiClient = OpenAiResponsesClient()

    var uiState by mutableStateOf(
        CoachUiState(settings = settingsStore.load())
    )
        private set

    fun updateMode(mode: CoachMode) {
        uiState = uiState.copy(mode = mode, userInput = mode.defaultPrompt)
    }

    fun updateUserInput(value: String) {
        uiState = uiState.copy(userInput = value)
    }

    fun updateSettings(settings: AiSettings) {
        settingsStore.save(settings)
        uiState = uiState.copy(settings = settings)
    }

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val attachments = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> resolver.toImageAttachment(uri) }
            }
            uiState = uiState.copy(images = (uiState.images + attachments).takeLast(6))
        }
    }

    fun clearImages() {
        uiState = uiState.copy(images = emptyList())
    }

    fun runAnalysis() {
        val current = uiState
        uiState = current.copy(isLoading = true, error = null, result = "")
        viewModelScope.launch {
            val instructions = promptBuilder.buildInstructions(current.mode)
            val prompt = promptBuilder.buildUserPrompt(current.mode, current.userInput)
            val result = apiClient.analyze(
                settings = current.settings,
                instructions = instructions,
                prompt = prompt,
                images = current.images
            )
            uiState = result.fold(
                onSuccess = { current.copy(isLoading = false, result = it, error = null) },
                onFailure = { current.copy(isLoading = false, result = "", error = it.message) }
            )
        }
    }
}

private class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    fun load(): AiSettings {
        return AiSettings(
            baseUrl = prefs.getString("base_url", AiSettings().baseUrl) ?: AiSettings().baseUrl,
            apiKey = prefs.getString("api_key", "") ?: "",
            model = prefs.getString("model", AiSettings().model) ?: AiSettings().model
        )
    }

    fun save(settings: AiSettings) {
        prefs.edit()
            .putString("base_url", settings.baseUrl)
            .putString("api_key", settings.apiKey)
            .putString("model", settings.model)
            .apply()
    }
}

private fun ContentResolver.toImageAttachment(uri: Uri): ImageAttachment? {
    val mimeType = getType(uri) ?: "image/jpeg"
    val name = query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else uri.lastPathSegment
    } ?: uri.lastPathSegment ?: "image"

    val bytes = openInputStream(uri)?.use { it.readBytes() } ?: return null
    return ImageAttachment(
        name = name,
        mimeType = mimeType,
        base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
    )
}
