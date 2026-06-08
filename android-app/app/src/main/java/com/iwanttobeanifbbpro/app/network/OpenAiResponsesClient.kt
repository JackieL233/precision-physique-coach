package com.iwanttobeanifbbpro.app.network

import com.iwanttobeanifbbpro.app.ui.AiSettings
import com.iwanttobeanifbbpro.app.ui.ImageAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAiResponsesClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun analyze(
        settings: AiSettings,
        instructions: String,
        prompt: String,
        images: List<ImageAttachment>
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(settings.apiKey.isNotBlank()) { "API key is required." }
            val url = settings.baseUrl.trimEnd('/').let { baseUrl ->
                if (baseUrl.endsWith("/responses")) baseUrl else "$baseUrl/responses"
            }
            val body = buildRequestBody(settings.model, instructions, prompt, images)
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${settings.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("HTTP ${response.code}: $responseBody")
                }
                parseOutputText(JSONObject(responseBody))
            }
        }
    }

    private fun buildRequestBody(
        model: String,
        instructions: String,
        prompt: String,
        images: List<ImageAttachment>
    ): JSONObject {
        val content = JSONArray()
            .put(JSONObject().put("type", "input_text").put("text", prompt))

        images.forEach { image ->
            val dataUrl = "data:${image.mimeType};base64,${image.base64}"
            content.put(
                JSONObject()
                    .put("type", "input_image")
                    .put("image_url", dataUrl)
                    .put("detail", "auto")
            )
        }

        val input = JSONArray()
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("content", content)
            )

        return JSONObject()
            .put("model", model)
            .put("instructions", instructions)
            .put("input", input)
    }

    private fun parseOutputText(json: JSONObject): String {
        val direct = json.optString("output_text")
        if (direct.isNotBlank()) return direct

        val output = json.optJSONArray("output") ?: return json.toString(2)
        val parts = mutableListOf<String>()
        for (i in 0 until output.length()) {
            val item = output.optJSONObject(i) ?: continue
            val content = item.optJSONArray("content") ?: continue
            for (j in 0 until content.length()) {
                val block = content.optJSONObject(j) ?: continue
                val text = block.optString("text").ifBlank {
                    block.optString("output_text")
                }
                if (text.isNotBlank()) parts += text
            }
        }
        return parts.joinToString("\n\n").ifBlank { json.toString(2) }
    }
}
