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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iwanttobeanifbbpro.app.core.CoachMode

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
        item {
            Header()
        }
        item {
            SettingsCard(
                settings = state.settings,
                onChange = viewModel::updateSettings
            )
        }
        item {
            ModePicker(
                selected = state.mode,
                onSelect = viewModel::updateMode
            )
        }
        item {
            PromptCard(
                text = state.userInput,
                onTextChange = viewModel::updateUserInput
            )
        }
        item {
            ImageCard(
                images = state.images,
                onPick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClear = viewModel::clearImages
            )
        }
        item {
            ActionCard(
                isLoading = state.isLoading,
                onRun = viewModel::runAnalysis
            )
        }
        state.error?.let { error ->
            item {
                MessageCard(title = "Error", body = error)
            }
        }
        if (state.result.isNotBlank()) {
            item {
                MessageCard(title = "AI analysis", body = state.result)
            }
        }
        item {
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
            text = "Professional-level physique planning powered by the bundled skill and your configurable AI API.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SettingsCard(settings: AiSettings, onChange: (AiSettings) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("API settings", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.baseUrl,
                onValueChange = { onChange(settings.copy(baseUrl = it)) },
                label = { Text("base URL") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.model,
                onValueChange = { onChange(settings.copy(model = it)) },
                label = { Text("model") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = settings.apiKey,
                onValueChange = { onChange(settings.copy(apiKey = it)) },
                label = { Text("API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ModePicker(selected: CoachMode, onSelect: (CoachMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Coach mode", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachMode.entries.forEach { mode ->
                FilterChip(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    label = { Text(mode.title) }
                )
            }
        }
    }
}

@Composable
private fun PromptCard(text: String, onTextChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        value = text,
        onValueChange = onTextChange,
        label = { Text("Your profile, log, question, or check-in") },
        minLines = 6
    )
}

@Composable
private fun ImageCard(
    images: List<ImageAttachment>,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = onPick) {
                    Text("Add photos")
                }
                TextButton(onClick = onClear, enabled = images.isNotEmpty()) {
                    Text("Clear")
                }
            }
            if (images.isEmpty()) {
                Text("Attach exercise frames, equipment photos, food photos, labels, or menu screenshots for multimodal analysis.")
            } else {
                images.forEach { image ->
                    Text("${image.name} (${image.mimeType})")
                }
            }
        }
    }
}

@Composable
private fun ActionCard(isLoading: Boolean, onRun: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            onClick = onRun
        ) {
            Text(if (isLoading) "Analyzing..." else "Run AI analysis")
        }
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MessageCard(title: String, body: String) {
    Card {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
