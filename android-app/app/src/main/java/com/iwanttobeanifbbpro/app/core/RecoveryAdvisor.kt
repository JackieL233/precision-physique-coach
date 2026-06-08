package com.iwanttobeanifbbpro.app.core

import com.iwanttobeanifbbpro.app.data.DailyLog

data class RecoveryGuidance(
    val statusLabel: String,
    val readinessScore: Int,
    val trainingPressure: String,
    val sleepSignal: String,
    val stressSignal: String,
    val sorenessSignal: String,
    val heartRateSignal: String,
    val recommendedTrainingAction: String,
    val rationale: String,
    val nextAction: String
)

fun recoveryGuidance(log: DailyLog, recentLogs: List<DailyLog>): RecoveryGuidance {
    val metrics = log.metrics
    val sleepPenalty = when {
        metrics.sleepHours == null -> 4
        metrics.sleepHours < 5.5 -> 22
        metrics.sleepHours < 6.5 -> 12
        metrics.sleepHours < 7.0 -> 5
        else -> 0
    }
    val sleepSignal = when {
        metrics.sleepHours == null -> "Sleep not logged"
        metrics.sleepHours < 5.5 -> "Short sleep"
        metrics.sleepHours < 6.5 -> "Below target"
        metrics.sleepHours < 7.0 -> "Acceptable"
        else -> "Good sleep"
    }

    val subjectivePenalty = listOf(metrics.fatigue, metrics.soreness, metrics.stress)
        .sumOf { (it - 3).coerceAtLeast(0) * 8 }
    val stressSignal = when {
        metrics.fatigue >= 5 || metrics.stress >= 5 -> "High fatigue/stress"
        metrics.fatigue >= 4 || metrics.stress >= 4 -> "Elevated fatigue/stress"
        metrics.fatigue <= 2 && metrics.stress <= 2 -> "Low fatigue/stress"
        else -> "Moderate fatigue/stress"
    }
    val sorenessSignal = when {
        metrics.soreness >= 5 -> "Severe soreness"
        metrics.soreness >= 4 -> "Elevated soreness"
        metrics.soreness <= 2 -> "Low soreness"
        else -> "Moderate soreness"
    }

    val recentHeartRateAverage = recentLogs
        .filter { it.date != log.date }
        .sortedByDescending { it.date }
        .take(10)
        .mapNotNull { it.metrics.restingHeartRateBpm }
        .takeIf { it.size >= 3 }
        ?.average()
    val heartRateDelta = if (metrics.restingHeartRateBpm != null && recentHeartRateAverage != null) {
        metrics.restingHeartRateBpm - recentHeartRateAverage
    } else {
        null
    }
    val heartRatePenalty = heartRateDelta?.let { delta ->
        when {
            delta >= 10.0 -> 18
            delta >= 7.0 -> 12
            delta >= 5.0 -> 6
            else -> 0
        }
    } ?: 0
    val heartRateSignal = when {
        metrics.restingHeartRateBpm == null -> "HR not logged"
        recentHeartRateAverage == null -> "HR baseline building"
        heartRateDelta != null && heartRateDelta >= 10.0 -> "HR sharply above baseline"
        heartRateDelta != null && heartRateDelta >= 7.0 -> "HR above baseline"
        heartRateDelta != null && heartRateDelta >= 5.0 -> "HR slightly above baseline"
        else -> "HR near baseline"
    }

    val plannedSets = log.plannedHardSets()
    val completedSets = log.completedHardSets()
    val completionPressure = when {
        plannedSets >= 20 -> 8
        completedSets >= 18 -> 8
        completedSets >= 14 -> 4
        else -> 0
    }
    val trainingPressure = when {
        plannedSets == 0 -> "No session pressure"
        completedSets >= 18 || plannedSets >= 20 -> "High training pressure"
        completedSets >= 10 -> "Moderate training pressure"
        completedSets > 0 -> "Low training pressure"
        else -> "Planned but not executed"
    }

    val score = (88 - sleepPenalty - subjectivePenalty - heartRatePenalty - completionPressure).coerceIn(25, 96)
    val statusLabel = when {
        score >= 82 && subjectivePenalty == 0 && heartRatePenalty <= 6 -> "Controlled push"
        score >= 68 -> "Hold training stress"
        score >= 52 -> "Reduce volume"
        else -> "Deload check"
    }
    val recommendedTrainingAction = when (statusLabel) {
        "Controlled push" -> "Keep planned training; progress only clean exercises with supportive history and RIR."
        "Hold training stress" -> "Keep load and sets stable; avoid adding extra sets or failure work today."
        "Reduce volume" -> "Trim 20-30% of hard sets or swap high-fatigue lifts for stable alternatives."
        else -> "Use a deload-style session, rest day, or technique-only work until signals improve."
    }
    val rationale = listOf(
        sleepSignal,
        stressSignal,
        sorenessSignal,
        heartRateSignal,
        trainingPressure
    ).joinToString("; ")
    val nextAction = when {
        metrics.sleepHours == null || metrics.restingHeartRateBpm == null ->
            "Sync Health Connect or enter sleep and resting HR so recovery guidance becomes trend-aware."
        statusLabel == "Controlled push" ->
            "Run AI review after logging all sets so progression stays exercise-specific."
        statusLabel == "Hold training stress" ->
            "Finish the planned workout without extra volume, then reassess tomorrow."
        statusLabel == "Reduce volume" ->
            "Prioritize sleep, hydration, warm-up quality, and lower-risk exercise choices."
        else ->
            "Treat this as a training-pressure warning, not a diagnosis; seek professional help for unusual symptoms or persistent pain."
    }
    return RecoveryGuidance(
        statusLabel = statusLabel,
        readinessScore = score,
        trainingPressure = trainingPressure,
        sleepSignal = sleepSignal,
        stressSignal = stressSignal,
        sorenessSignal = sorenessSignal,
        heartRateSignal = heartRateSignal,
        recommendedTrainingAction = recommendedTrainingAction,
        rationale = rationale,
        nextAction = nextAction
    )
}
