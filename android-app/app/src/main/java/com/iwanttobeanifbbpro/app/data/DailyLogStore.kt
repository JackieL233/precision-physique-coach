package com.iwanttobeanifbbpro.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class DailyLogStore(context: Context) {
    private val prefs = context.getSharedPreferences("daily_logs", Context.MODE_PRIVATE)

    fun readLog(): DailyLog {
        val today = LocalDate.now().toString()
        val raw = prefs.getString(logKey(today), null) ?: prefs.getString("today_log", null) ?: return DailyLog()
        return runCatching { DailyLog.fromJson(JSONObject(raw)) }.getOrElse { DailyLog() }
    }

    fun readRecentLogs(days: Int = 14): List<DailyLog> {
        val indexedDates = readDateIndex()
        if (indexedDates.isEmpty()) {
            return prefs.getString("today_log", null)?.let { raw ->
                runCatching { listOf(DailyLog.fromJson(JSONObject(raw))) }.getOrElse { emptyList() }
            } ?: emptyList()
        }
        return indexedDates
            .sortedDescending()
            .take(days.coerceAtLeast(1))
            .mapNotNull { date ->
                prefs.getString(logKey(date), null)?.let { raw ->
                    runCatching { DailyLog.fromJson(JSONObject(raw)) }.getOrNull()
                }
            }
            .sortedBy { it.date }
    }

    fun saveLog(log: DailyLog) {
        val raw = log.toJson().toString()
        val dates = (readDateIndex() + log.date).distinct().sortedDescending().take(MAX_STORED_DAYS)
        prefs.edit()
            .putString("today_log", raw)
            .putString(logKey(log.date), raw)
            .putString(DATE_INDEX_KEY, JSONArray(dates).toString())
            .apply()
    }

    fun resetToday() {
        saveLog(DailyLog())
    }

    private fun readDateIndex(): List<String> {
        val raw = prefs.getString(DATE_INDEX_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index -> array.optString(index).takeIf { it.isNotBlank() } }
        }.getOrElse { emptyList() }
    }

    private fun logKey(date: String): String = "log_$date"

    companion object {
        private const val DATE_INDEX_KEY = "log_date_index"
        private const val MAX_STORED_DAYS = 180
    }
}
