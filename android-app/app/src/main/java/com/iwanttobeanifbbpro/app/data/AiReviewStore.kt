package com.iwanttobeanifbbpro.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class AiReviewEntry(
    val createdAt: String = Instant.now().toString(),
    val logDate: String,
    val modeTitle: String,
    val body: String
) {
    fun preview(maxLength: Int = 220): String {
        val compact = body.lines().map { it.trim() }.filter { it.isNotBlank() }.joinToString(" ")
        return if (compact.length <= maxLength) compact else compact.take(maxLength).trimEnd() + "..."
    }

    companion object
}

class AiReviewStore(context: Context) {
    private val prefs = context.getSharedPreferences("ai_review_history", Context.MODE_PRIVATE)

    fun readReviews(): List<AiReviewEntry> {
        val raw = prefs.getString(REVIEWS_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let { AiReviewEntry.fromJson(it) }
            }
        }.getOrElse { emptyList() }
    }

    fun latest(): AiReviewEntry? = readReviews().firstOrNull()

    fun saveReview(entry: AiReviewEntry) {
        val reviews = (listOf(entry) + readReviews()).take(MAX_REVIEWS)
        prefs.edit()
            .putString(REVIEWS_KEY, JSONArray().also { array -> reviews.forEach { array.put(it.toJson()) } }.toString())
            .apply()
    }

    companion object {
        private const val REVIEWS_KEY = "reviews"
        private const val MAX_REVIEWS = 10
    }
}

private fun AiReviewEntry.toJson(): JSONObject {
    return JSONObject()
        .put("createdAt", createdAt)
        .put("logDate", logDate)
        .put("modeTitle", modeTitle)
        .put("body", body)
}

fun AiReviewEntry.Companion.fromJson(json: JSONObject): AiReviewEntry {
    return AiReviewEntry(
        createdAt = json.optString("createdAt", Instant.now().toString()),
        logDate = json.optString("logDate", ""),
        modeTitle = json.optString("modeTitle", ""),
        body = json.optString("body", "")
    )
}
