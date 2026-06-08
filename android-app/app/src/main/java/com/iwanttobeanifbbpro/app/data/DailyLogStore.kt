package com.iwanttobeanifbbpro.app.data

import android.content.Context
import org.json.JSONObject

class DailyLogStore(context: Context) {
    private val prefs = context.getSharedPreferences("daily_logs", Context.MODE_PRIVATE)

    fun readLog(): DailyLog {
        val raw = prefs.getString("today_log", null) ?: return DailyLog()
        return runCatching { DailyLog.fromJson(JSONObject(raw)) }.getOrElse { DailyLog() }
    }

    fun saveLog(log: DailyLog) {
        prefs.edit()
            .putString("today_log", log.toJson().toString())
            .apply()
    }

    fun resetToday() {
        saveLog(DailyLog())
    }
}
