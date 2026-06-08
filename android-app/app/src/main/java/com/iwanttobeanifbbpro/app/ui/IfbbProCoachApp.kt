package com.iwanttobeanifbbpro.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iwanttobeanifbbpro.app.core.CoachMode
import com.iwanttobeanifbbpro.app.data.DailyMetrics
import com.iwanttobeanifbbpro.app.data.DailyTargets

@Composable
fun IfbbProCoachApp(viewModel: CoachViewModel = viewModel()) {
    val state = viewModel.uiState
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6),
        onResult = viewModel::addImages
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header() }
        item { TabPicker(selected = state.selectedTab, onSelect = viewModel::selectTab) }
        when (state.selectedTab) {
            AppTab.TODAY -> {
                item { TodayDashboard(state = state, onDailyReview = viewModel::runDailyReview, onReset = viewModel::resetToday) }
            }
            AppTab.TRAINING -> {
                item {
                    TrainingPage(
                        state = state,
                        onFocusChange = viewModel::updateTrainingFocus,
                        onNotesChange = viewModel::updateSessionNotes,
                        onCompletedChange = { viewModel.toggleTrainingCompleted() },
                        onAddExercise = viewModel::addExercise,
                        onRemoveExercise = viewModel::removeExercise
                    )
                }
            }
            AppTab.NUTRITION -> {
                item {
                    NutritionPage(
                        state = state,
                        onTargetsChange = viewModel::updateTargets,
                        onAddMeal = viewModel::addMeal,
                        onRemoveMeal = viewModel::removeMeal,
                        onPickMealPhoto = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
            AppTab.METRICS -> {
                item {
                    MetricsPage(
                        state = state,
                        onMetricsChange = viewModel::updateMetrics,
                        onReflectionChange = viewModel::updateReflection
                    )
                }
            }
            AppTab.AI_COACH -> {
                item {
                    AiCoachPage(
                        state = state,
                        onSettingsChange = viewModel::updateSettings,
                        onModeChange = viewModel::updateMode,
                        onPromptChange = viewModel::updateUserInput,
                        onPickImages = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onClearImages = viewModel::clearImages,
                        onRunAnalysis = viewModel::runAnalysis,
                        onDailyReview = viewModel::runDailyReview
                    )
                }
            }
        }
        item {
            state.error?.let { MessageCard(title = "Error", body = it) }
            if (state.result.isNotBlank()) {
                MessageCard(title = "AI Coach result", body = state.result)
            }
            SafetyNote()
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "I Want to be an IFBB PRO",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Daily training, daily nutrition, metrics, photos, and AI review for tomorrow's training and diet adjustment.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TabPicker(selected: AppTab, onSelect: (AppTab) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTab.entries.forEach { tab ->
            FilterChip(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                label = { Text(tab.title) }
            )
        }
    }
}

@Composable
private fun TodayDashboard(
    state: CoachUiState,
    onDailyReview: () -> Unit,
    onReset: () -> Unit
) {
    val log = state.dailyLog
    val totals = log.nutritionTotals()
    Card {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Today", fontWeight = FontWeight.SemiBold)
            Text("Training: ${log.trainingSession.plannedFocus} | completed: ${log.trainingSession.completed}")
            Text("Exercises: ${log.trainingSession.exercises.size} | hard sets: ${log.trainingSession.exercises.sumOf { it.sets }}")
            Text("Nutrition: ${totals.calories}/${log.targets.calories} kcal | P ${totals.protein}/${log.targets.protein}g | C ${totals.carbs}/${log.targets.carbs}g | F ${totals.fat}/${log.targets.fat}g")
            Text("Metrics: ${log.metrics.bodyWeightKg ?: "--"} kg, waist ${log.metrics.waistCm ?: "--"} cm, sleep ${log.metrics.sleepHours ?: "--"} h, steps ${log.metrics.steps}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDailyReview, enabled = !state.isLoading) {
                    Text(if (state.isLoading) "Reviewing..." else "AI review tomorrow")
                }
                TextButton(onClick = onReset) {
                    Text("Reset today")
                }
            }
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TrainingPage(
    state: CoachUiState,
    onFocusChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompletedChange: () -> Unit,
    onAddExercise: (String, String, Int, String, Double?, Double?, String) -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("8-12") }
    var load by remember { mutableStateOf("") }
    var rir by remember { mutableStateOf("2") }
    var notes by remember { mutableStateOf("") }
    val session = state.dailyLog.trainingSession

    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Training", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(session.plannedFocus, onFocusChange, Modifier.fillMaxWidth(), label = { Text("Planned focus") })
            Row {
                Checkbox(checked = session.completed, onCheckedChange = { onCompletedChange() })
                Text("Training completed")
            }
            Text("Log exercise, hard sets, RIR, load, target muscle, pain or form notes.")
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Exercise") })
            OutlinedTextField(muscle, { muscle = it }, Modifier.fillMaxWidth(), label = { Text("Target muscle") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(sets, { sets = it }, Modifier.weight(1f), label = { Text("Sets") })
                OutlinedTextField(reps, { reps = it }, Modifier.weight(1f), label = { Text("Reps") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(load, { load = it }, Modifier.weight(1f), label = { Text("Load kg") })
                OutlinedTextField(rir, { rir = it }, Modifier.weight(1f), label = { Text("RIR") })
            }
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Technique, stimulus, pain") })
            Button(
                onClick = {
                    onAddExercise(name, muscle, sets.toIntOrNull() ?: 0, reps, load.toDoubleOrNull(), rir.toDoubleOrNull(), notes)
                    name = ""; muscle = ""; notes = ""
                }
            ) {
                Text("Add exercise")
            }
            OutlinedTextField(session.sessionNotes, onNotesChange, Modifier.fillMaxWidth(), label = { Text("Session notes") })
            state.dailyLog.trainingSession.exercises.forEachIndexed { index, exercise ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${exercise.name}: ${exercise.sets} x ${exercise.reps}, ${exercise.targetMuscle}", Modifier.weight(1f))
                    TextButton(onClick = { onRemoveExercise(index) }) { Text("Remove") }
                }
            }
        }
    }
}

@Composable
private fun NutritionPage(
    state: CoachUiState,
    onTargetsChange: (DailyTargets) -> Unit,
    onAddMeal: (String, Int, Double, Double, Double, Double, String) -> Unit,
    onRemoveMeal: (Int) -> Unit,
    onPickMealPhoto: () -> Unit
) {
    val targets = state.dailyLog.targets
    var mealName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val totals = state.dailyLog.nutritionTotals()

    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Nutrition", fontWeight = FontWeight.SemiBold)
            Text("Daily nutrition: ${totals.calories}/${targets.calories} kcal, P ${totals.protein}/${targets.protein}g, C ${totals.carbs}/${targets.carbs}g, F ${totals.fat}/${targets.fat}g")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(targets.calories.toString(), { onTargetsChange(targets.copy(calories = it.toIntOrNull() ?: targets.calories)) }, Modifier.weight(1f), label = { Text("Target kcal") })
                OutlinedTextField(targets.protein.toString(), { onTargetsChange(targets.copy(protein = it.toIntOrNull() ?: targets.protein)) }, Modifier.weight(1f), label = { Text("Protein") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(targets.carbs.toString(), { onTargetsChange(targets.copy(carbs = it.toIntOrNull() ?: targets.carbs)) }, Modifier.weight(1f), label = { Text("Carbs") })
                OutlinedTextField(targets.fat.toString(), { onTargetsChange(targets.copy(fat = it.toIntOrNull() ?: targets.fat)) }, Modifier.weight(1f), label = { Text("Fat") })
            }
            OutlinedTextField(mealName, { mealName = it }, Modifier.fillMaxWidth(), label = { Text("Meal") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(calories, { calories = it }, Modifier.weight(1f), label = { Text("kcal") })
                OutlinedTextField(protein, { protein = it }, Modifier.weight(1f), label = { Text("Protein") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(carbs, { carbs = it }, Modifier.weight(1f), label = { Text("Carbs") })
                OutlinedTextField(fat, { fat = it }, Modifier.weight(1f), label = { Text("Fat") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(fiber, { fiber = it }, Modifier.weight(1f), label = { Text("Fiber") })
                ElevatedButton(onClick = onPickMealPhoto, modifier = Modifier.weight(1f)) { Text("Add meal photo") }
            }
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Food source, portion, oils, label") })
            Button(
                onClick = {
                    onAddMeal(mealName, calories.toIntOrNull() ?: 0, protein.toDoubleOrNull() ?: 0.0, carbs.toDoubleOrNull() ?: 0.0, fat.toDoubleOrNull() ?: 0.0, fiber.toDoubleOrNull() ?: 0.0, notes)
                    mealName = ""; calories = ""; protein = ""; carbs = ""; fat = ""; fiber = ""; notes = ""
                }
            ) { Text("Add meal") }
            state.dailyLog.meals.forEachIndexed { index, meal ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${meal.name}: ${meal.calories} kcal, P ${meal.protein}g C ${meal.carbs}g F ${meal.fat}g", Modifier.weight(1f))
                    TextButton(onClick = { onRemoveMeal(index) }) { Text("Remove") }
                }
            }
        }
    }
}

@Composable
private fun MetricsPage(
    state: CoachUiState,
    onMetricsChange: (DailyMetrics) -> Unit,
    onReflectionChange: (String) -> Unit
) {
    val metrics = state.dailyLog.metrics
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Metrics", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(metrics.bodyWeightKg?.toString().orEmpty(), { onMetricsChange(metrics.copy(bodyWeightKg = it.toDoubleOrNull())) }, Modifier.weight(1f), label = { Text("Weight kg") })
                OutlinedTextField(metrics.waistCm?.toString().orEmpty(), { onMetricsChange(metrics.copy(waistCm = it.toDoubleOrNull())) }, Modifier.weight(1f), label = { Text("Waist cm") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(metrics.sleepHours?.toString().orEmpty(), { onMetricsChange(metrics.copy(sleepHours = it.toDoubleOrNull())) }, Modifier.weight(1f), label = { Text("Sleep h") })
                OutlinedTextField(metrics.steps.toString(), { onMetricsChange(metrics.copy(steps = it.toIntOrNull() ?: metrics.steps)) }, Modifier.weight(1f), label = { Text("Steps") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(metrics.hunger.toString(), { onMetricsChange(metrics.copy(hunger = it.toIntOrNull() ?: metrics.hunger)) }, Modifier.weight(1f), label = { Text("Hunger 1-5") })
                OutlinedTextField(metrics.fatigue.toString(), { onMetricsChange(metrics.copy(fatigue = it.toIntOrNull() ?: metrics.fatigue)) }, Modifier.weight(1f), label = { Text("Fatigue 1-5") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(metrics.soreness.toString(), { onMetricsChange(metrics.copy(soreness = it.toIntOrNull() ?: metrics.soreness)) }, Modifier.weight(1f), label = { Text("Soreness 1-5") })
                OutlinedTextField(metrics.stress.toString(), { onMetricsChange(metrics.copy(stress = it.toIntOrNull() ?: metrics.stress)) }, Modifier.weight(1f), label = { Text("Stress 1-5") })
            }
            OutlinedTextField(state.dailyLog.reflection, onReflectionChange, Modifier.fillMaxWidth(), label = { Text("Daily reflection") })
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AiCoachPage(
    state: CoachUiState,
    onSettingsChange: (AiSettings) -> Unit,
    onModeChange: (CoachMode) -> Unit,
    onPromptChange: (String) -> Unit,
    onPickImages: () -> Unit,
    onClearImages: () -> Unit,
    onRunAnalysis: () -> Unit,
    onDailyReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsCard(settings = state.settings, onChange = onSettingsChange)
        Text("AI Coach", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachMode.entries.forEach { mode ->
                FilterChip(selected = state.mode == mode, onClick = { onModeChange(mode) }, label = { Text(mode.title) })
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            value = state.userInput,
            onValueChange = onPromptChange,
            label = { Text("Question, goal, or extra AI review instruction") },
            minLines = 5
        )
        ImageCard(images = state.images, onPick = onPickImages, onClear = onClearImages)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDailyReview, enabled = !state.isLoading, modifier = Modifier.weight(1f)) { Text("AI review tomorrow") }
            ElevatedButton(onClick = onRunAnalysis, enabled = !state.isLoading, modifier = Modifier.weight(1f)) { Text("Run mode") }
        }
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SettingsCard(settings: AiSettings, onChange: (AiSettings) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("API settings", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(settings.baseUrl, { onChange(settings.copy(baseUrl = it)) }, Modifier.fillMaxWidth(), label = { Text("base URL") }, singleLine = true)
            OutlinedTextField(settings.model, { onChange(settings.copy(model = it)) }, Modifier.fillMaxWidth(), label = { Text("model") }, singleLine = true)
            OutlinedTextField(settings.apiKey, { onChange(settings.copy(apiKey = it)) }, Modifier.fillMaxWidth(), label = { Text("API key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        }
    }
}

@Composable
private fun ImageCard(images: List<ImageAttachment>, onPick: () -> Unit, onClear: () -> Unit) {
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = onPick) { Text("Add photos") }
                TextButton(onClick = onClear, enabled = images.isNotEmpty()) { Text("Clear") }
            }
            if (images.isEmpty()) {
                Text("Attach exercise frames, equipment photos, food photos, meal photo labels, or menu screenshots.")
            } else {
                images.forEach { image -> Text("${image.name} (${image.mimeType})") }
            }
        }
    }
}

@Composable
private fun MessageCard(title: String, body: String) {
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body)
        }
    }
}

@Composable
private fun SafetyNote() {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "This app is coaching support, not medical care. Photo form checks and food estimates are approximate.",
        style = MaterialTheme.typography.bodySmall
    )
}
