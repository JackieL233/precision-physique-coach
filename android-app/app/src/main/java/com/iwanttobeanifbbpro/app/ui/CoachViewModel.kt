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
import com.iwanttobeanifbbpro.app.core.DailySummaryBuilder
import com.iwanttobeanifbbpro.app.core.SkillAssetRepository
import com.iwanttobeanifbbpro.app.core.SkillPromptBuilder
import com.iwanttobeanifbbpro.app.data.DailyLog
import com.iwanttobeanifbbpro.app.data.DailyLogStore
import com.iwanttobeanifbbpro.app.data.DailyMetrics
import com.iwanttobeanifbbpro.app.data.DailyTargets
import com.iwanttobeanifbbpro.app.data.ExerciseEntry
import com.iwanttobeanifbbpro.app.data.MealEntry
import com.iwanttobeanifbbpro.app.network.OpenAiResponsesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab(val title: String) {
    TODAY("Today"),
    TRAINING("Training"),
    NUTRITION("Nutrition"),
    METRICS("Metrics"),
    AI_COACH("AI Coach")
}

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
    val selectedTab: AppTab = AppTab.TODAY,
    val mode: CoachMode = CoachMode.LINKED_TRAINING_NUTRITION,
    val userInput: String = CoachMode.LINKED_TRAINING_NUTRITION.defaultPrompt,
    val settings: AiSettings = AiSettings(),
    val dailyLog: DailyLog = DailyLog(),
    val images: List<ImageAttachment> = emptyList(),
    val result: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CoachViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)
    private val dailyLogStore = DailyLogStore(application)
    private val dailySummaryBuilder = DailySummaryBuilder()
    private val promptBuilder = SkillPromptBuilder(SkillAssetRepository(application))
    private val apiClient = OpenAiResponsesClient()

    var uiState by mutableStateOf(
        CoachUiState(
            settings = settingsStore.load(),
            dailyLog = dailyLogStore.readLog()
        )
    )
        private set

    fun selectTab(tab: AppTab) {
        uiState = uiState.copy(selectedTab = tab)
    }

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

    fun updateTargets(targets: DailyTargets) {
        updateLog(uiState.dailyLog.copy(targets = targets))
    }

    fun updateMetrics(metrics: DailyMetrics) {
        updateLog(uiState.dailyLog.copy(metrics = metrics))
    }

    fun updateTrainingFocus(focus: String) {
        val session = uiState.dailyLog.trainingSession.copy(plannedFocus = focus)
        updateLog(uiState.dailyLog.copy(trainingSession = session))
    }

    fun updateSessionNotes(notes: String) {
        val session = uiState.dailyLog.trainingSession.copy(sessionNotes = notes)
        updateLog(uiState.dailyLog.copy(trainingSession = session))
    }

    fun toggleTrainingCompleted() {
        val session = uiState.dailyLog.trainingSession
        updateLog(uiState.dailyLog.copy(trainingSession = session.copy(completed = !session.completed)))
    }

    fun addExercise(
        name: String,
        targetMuscle: String,
        sets: Int,
        reps: String,
        loadKg: Double?,
        rir: Double?,
        notes: String
    ) {
        if (name.isBlank()) return
        val entry = ExerciseEntry(
            name = name.trim(),
            targetMuscle = targetMuscle.trim(),
            sets = sets.coerceAtLeast(0),
            reps = reps.trim(),
            loadKg = loadKg,
            rir = rir,
            notes = notes.trim()
        )
        val session = uiState.dailyLog.trainingSession
        updateLog(uiState.dailyLog.copy(trainingSession = session.copy(exercises = session.exercises + entry)))
    }

    fun removeExercise(index: Int) {
        val session = uiState.dailyLog.trainingSession
        val exercises = session.exercises.filterIndexed { itemIndex, _ -> itemIndex != index }
        updateLog(uiState.dailyLog.copy(trainingSession = session.copy(exercises = exercises)))
    }

    fun addMeal(
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        notes: String
    ) {
        if (name.isBlank()) return
        val meal = MealEntry(
            name = name.trim(),
            calories = calories.coerceAtLeast(0),
            protein = protein.coerceAtLeast(0.0),
            carbs = carbs.coerceAtLeast(0.0),
            fat = fat.coerceAtLeast(0.0),
            fiber = fiber.coerceAtLeast(0.0),
            notes = notes.trim()
        )
        updateLog(uiState.dailyLog.copy(meals = uiState.dailyLog.meals + meal))
    }

    fun removeMeal(index: Int) {
        updateLog(uiState.dailyLog.copy(meals = uiState.dailyLog.meals.filterIndexed { itemIndex, _ -> itemIndex != index }))
    }

    fun updateReflection(reflection: String) {
        updateLog(uiState.dailyLog.copy(reflection = reflection))
    }

    fun resetToday() {
        dailyLogStore.resetToday()
        uiState = uiState.copy(dailyLog = dailyLogStore.readLog(), result = "", error = null)
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
        runAi(mode = uiState.mode, request = uiState.userInput)
    }

    fun runDailyReview() {
        val context = dailySummaryBuilder.buildAiReviewContext(
            log = uiState.dailyLog,
            extraRequest = uiState.userInput
        )
        runAi(mode = CoachMode.CHECK_IN, request = context)
    }

    private fun runAi(mode: CoachMode, request: String) {
        val current = uiState
        uiState = current.copy(isLoading = true, error = null, result = "")
        viewModelScope.launch {
            val instructions = promptBuilder.buildInstructions(mode)
            val prompt = promptBuilder.buildUserPrompt(mode, request)
            val result = apiClient.analyze(
                settings = current.settings,
                instructions = instructions,
                prompt = prompt,
                images = current.images
            )
            uiState = result.fold(
                onSuccess = { uiState.copy(isLoading = false, result = it, error = null) },
                onFailure = { uiState.copy(isLoading = false, result = "", error = it.message) }
            )
        }
    }

    private fun updateLog(log: DailyLog) {
        dailyLogStore.saveLog(log)
        uiState = uiState.copy(dailyLog = log)
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
