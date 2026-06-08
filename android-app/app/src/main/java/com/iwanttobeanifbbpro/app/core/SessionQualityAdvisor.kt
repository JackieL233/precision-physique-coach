package com.iwanttobeanifbbpro.app.core

import com.iwanttobeanifbbpro.app.data.DailyLog
import java.util.Locale

data class MuscleVolumeShare(
    val targetMuscle: String,
    val completedSets: Int,
    val volumeKg: Double
)

data class SessionQualityDashboard(
    val statusLabel: String,
    val qualityScore: Int,
    val completionRatePercent: Int,
    val plannedSets: Int,
    val completedSets: Int,
    val completedVolumeKg: Double,
    val averageRir: Double?,
    val loggedSetRatePercent: Int,
    val painFlagCount: Int,
    val techniqueFlagCount: Int,
    val topMuscleVolumes: List<MuscleVolumeShare>,
    val qualityCue: String,
    val capacityCue: String,
    val riskCue: String,
    val aiReviewFocus: String
) {
    fun promptLine(): String {
        val muscleLine = topMuscleVolumes.joinToString("; ") {
            "${it.targetMuscle}: ${it.completedSets} sets, ${it.volumeKg} kg"
        }.ifBlank { "No target muscle volume logged" }
        return "Session Quality Dashboard: $statusLabel, score $qualityScore, completion $completedSets/$plannedSets ($completionRatePercent%), logged set rate $loggedSetRatePercent%, volume $completedVolumeKg kg, average RIR ${averageRir ?: "not logged"}, pain flags $painFlagCount, technique flags $techniqueFlagCount | Muscle volume: $muscleLine | Quality cue: $qualityCue | Capacity cue: $capacityCue | Risk cue: $riskCue | AI review focus: $aiReviewFocus"
    }
}

fun sessionQualityDashboard(log: DailyLog): SessionQualityDashboard {
    val exercises = log.trainingSession.exercises
    val plannedSets = log.plannedHardSets()
    val completedSets = log.completedHardSets()
    val completionRate = if (plannedSets > 0) {
        (completedSets * 100 / plannedSets).coerceIn(0, 100)
    } else {
        0
    }
    val allSets = exercises.flatMap { it.trackedSets() }
    val completedSetRows = allSets.filter { it.completed }
    val loggedSetRate = if (completedSetRows.isNotEmpty()) {
        val loggedRows = completedSetRows.count { it.actualReps != null && it.loadKg != null && it.rir != null }
        (loggedRows * 100 / completedSetRows.size).coerceIn(0, 100)
    } else {
        0
    }
    val averageRir = completedSetRows.mapNotNull { it.rir }.takeIf { it.isNotEmpty() }?.average()
    val completedVolume = log.trainingVolumeKg()
    val painFlags = exercises.sumOf { exercise ->
        val exerciseFlag = if (hasPainSignal(exercise.notes)) 1 else 0
        exerciseFlag + exercise.trackedSets().count { hasPainSignal(it.notes) }
    }
    val techniqueFlags = exercises.sumOf { exercise ->
        val exerciseFlag = if (hasTechniqueFlag(exercise.notes)) 1 else 0
        exerciseFlag + exercise.trackedSets().count { hasTechniqueFlag(it.notes) }
    }
    val muscleVolumes = exercises
        .groupBy { it.targetMuscle.ifBlank { "Unassigned" } }
        .map { (muscle, muscleExercises) ->
            MuscleVolumeShare(
                targetMuscle = muscle,
                completedSets = muscleExercises.sumOf { it.completedSetCount() },
                volumeKg = muscleExercises.sumOf { it.volumeKg() }
            )
        }
        .filter { it.completedSets > 0 || it.volumeKg > 0.0 }
        .sortedWith(compareByDescending<MuscleVolumeShare> { it.completedSets }.thenByDescending { it.volumeKg })
        .take(5)

    val riskPenalty = painFlags * 16 + techniqueFlags * 6
    val completionPenalty = when {
        plannedSets == 0 -> 22
        completionRate >= 90 -> 0
        completionRate >= 75 -> 6
        completionRate >= 50 -> 14
        else -> 24
    }
    val loggingPenalty = when {
        completedSets == 0 -> 10
        loggedSetRate >= 85 -> 0
        loggedSetRate >= 60 -> 8
        else -> 16
    }
    val rirPenalty = when {
        averageRir == null -> 6
        averageRir < 0.5 -> 10
        averageRir > 4.0 -> 8
        else -> 0
    }
    val qualityScore = (94 - riskPenalty - completionPenalty - loggingPenalty - rirPenalty).coerceIn(20, 98)
    val statusLabel = when {
        plannedSets == 0 -> "No loaded session"
        painFlags > 0 -> "Pain check"
        techniqueFlags >= 2 -> "Technique watch"
        completedSets == 0 -> "Ready to log"
        completionRate < 75 -> "Incomplete session"
        loggedSetRate < 70 -> "Under-logged"
        averageRir != null && averageRir < 0.5 -> "High fatigue"
        qualityScore >= 86 -> "Productive session"
        qualityScore >= 72 -> "Usable session"
        else -> "Review before progressing"
    }
    val qualityCue = when (statusLabel) {
        "No loaded session" -> "Apply a plan day or add exercises before judging training quality."
        "Pain check" -> "Do not progress painful movements; review setup, range, and substitutions first."
        "Technique watch" -> "Keep load stable until technique notes are clean and repeatable."
        "Incomplete session" -> "Finish planned working sets before using this session for progression."
        "Under-logged" -> "Log load, reps, and RIR for completed sets so AI can judge progression."
        "High fatigue" -> "Treat near-failure work as a recovery cost; avoid adding volume today."
        "Productive session" -> "Session quality is strong enough for exercise-specific progression checks."
        "Usable session" -> "Use Exercise History and Progression Cue before changing load or volume."
        else -> "Review completion, RIR, pain, and technique before progressing."
    }
    val capacityCue = when {
        muscleVolumes.isEmpty() -> "No target-muscle capacity is available yet."
        muscleVolumes.size == 1 -> "${muscleVolumes.first().targetMuscle} received all completed work; confirm this matches today's focus."
        else -> {
            val lead = muscleVolumes.first()
            val secondary = muscleVolumes.drop(1).joinToString(", ") { "${it.targetMuscle} ${it.completedSets} sets" }
            "${lead.targetMuscle} leads with ${lead.completedSets} completed sets; secondary work: $secondary."
        }
    }
    val riskCue = when {
        painFlags > 0 -> "Pain notes override progression; consider swap, range change, deload, or professional assessment if persistent."
        techniqueFlags > 0 -> "Technique flags should be cleaned up before adding load."
        averageRir != null && averageRir < 0.5 -> "Average RIR is very low; recovery may limit the next session."
        else -> "No pain or major technique flags logged; keep notes honest."
    }
    val aiReviewFocus = "Use completion rate, logged set rate, average RIR, muscle-volume distribution, pain flags, technique flags, and Exercise History before changing the next plan."

    return SessionQualityDashboard(
        statusLabel = statusLabel,
        qualityScore = qualityScore,
        completionRatePercent = completionRate,
        plannedSets = plannedSets,
        completedSets = completedSets,
        completedVolumeKg = completedVolume,
        averageRir = averageRir,
        loggedSetRatePercent = loggedSetRate,
        painFlagCount = painFlags,
        techniqueFlagCount = techniqueFlags,
        topMuscleVolumes = muscleVolumes,
        qualityCue = qualityCue,
        capacityCue = capacityCue,
        riskCue = riskCue,
        aiReviewFocus = aiReviewFocus
    )
}

private fun hasPainSignal(text: String): Boolean {
    val clean = text.lowercase(Locale.US)
        .replace("pain-free", "painfree")
        .replace("pain free", "painfree")
        .replace("no pain", "nopain")
        .replace("without pain", "withoutpain")
        .replace("无痛", "nopain")
        .replace("没有痛", "nopain")
        .replace("不痛", "nopain")
        .replace("不疼", "nopain")
    return clean.contains("pain") ||
        clean.contains("ache") ||
        clean.contains("疼") ||
        clean.contains("痛")
}

private fun hasTechniqueFlag(text: String): Boolean {
    val clean = text.lowercase(Locale.US)
    return listOf(
        "form break",
        "technique",
        "unstable",
        "compensation",
        "cheat",
        "grind",
        "failed",
        "missed",
        "range short",
        "失控",
        "代偿",
        "动作变形"
    ).any { clean.contains(it) }
}
