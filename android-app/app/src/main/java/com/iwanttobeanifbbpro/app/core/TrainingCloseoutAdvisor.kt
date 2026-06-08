package com.iwanttobeanifbbpro.app.core

import com.iwanttobeanifbbpro.app.data.DailyLog
import com.iwanttobeanifbbpro.app.data.PhotoEvidenceType

data class TrainingCloseoutTask(
    val label: String,
    val status: String,
    val actionCue: String,
    val complete: Boolean
)

data class TrainingCloseoutCoach(
    val statusLabel: String,
    val closeoutScore: Int,
    val completedSets: Int,
    val plannedSets: Int,
    val missingLogCount: Int,
    val painFlagCount: Int,
    val techniqueFlagCount: Int,
    val photoCue: String,
    val nutritionCue: String,
    val metricsCue: String,
    val sessionNoteCue: String,
    val aiReviewGate: String,
    val primaryActionLabel: String,
    val primaryActionRoute: DailyExecutionRoute,
    val checklist: List<TrainingCloseoutTask>,
    val aiReviewFocus: String
) {
    fun promptLine(): String {
        val tasks = checklist.joinToString("; ") {
            "${it.label}: ${it.status}, complete ${it.complete}, action ${it.actionCue}"
        }
        return "Training Closeout Coach: $statusLabel, closeout score $closeoutScore, sets $completedSets/$plannedSets, missing set logs $missingLogCount, pain flags $painFlagCount, technique flags $techniqueFlagCount | Photo cue: $photoCue | Nutrition cue: $nutritionCue | Metrics cue: $metricsCue | Session note cue: $sessionNoteCue | AI review gate: $aiReviewGate | AI review readiness: $statusLabel / $closeoutScore | Primary action: $primaryActionLabel via $primaryActionRoute | Checklist: $tasks | AI review focus: $aiReviewFocus"
    }
}

fun trainingCloseoutCoach(
    log: DailyLog,
    hasAiReviewToday: Boolean = false
): TrainingCloseoutCoach {
    val dashboard = sessionQualityDashboard(log)
    val allSets = log.trainingSession.exercises.flatMap { it.trackedSets() }
    val completedRows = allSets.filter { it.completed }
    val missingLogCount = completedRows.count { it.actualReps == null || it.loadKg == null || it.rir == null }
    val trainingFormPhotos = log.photoEvidence.count { it.type == PhotoEvidenceType.TRAINING_FORM }
    val equipmentPhotos = log.photoEvidence.count { it.type == PhotoEvidenceType.EQUIPMENT }
    val hasFoodEvidence = log.meals.isNotEmpty() ||
        log.photoEvidence.any { it.type == PhotoEvidenceType.MEAL || it.type == PhotoEvidenceType.MENU_LABEL }
    val metricsReady = log.metrics.bodyWeightKg != null ||
        log.metrics.sleepHours != null ||
        log.metrics.steps > 0 ||
        log.metrics.restingHeartRateBpm != null ||
        log.metrics.healthSyncedAt.isNotBlank()
    val sessionNotesReady = log.trainingSession.sessionNotes.isNotBlank() ||
        log.trainingSession.exercises.any { exercise ->
            exercise.notes.isNotBlank() || exercise.trackedSets().any { it.notes.isNotBlank() }
        }
    val hasRiskFlags = dashboard.painFlagCount > 0 || dashboard.techniqueFlagCount > 0
    val needsPhotoForRisk = hasRiskFlags && trainingFormPhotos == 0
    val needsEquipmentPhoto = dashboard.techniqueFlagCount > 0 && equipmentPhotos == 0
    val plannedSets = dashboard.plannedSets
    val completedSets = dashboard.completedSets
    val trainingFinished = plannedSets > 0 && completedSets >= plannedSets

    val completionTask = TrainingCloseoutTask(
        label = "Finish working sets",
        status = when {
            plannedSets == 0 -> "No loaded workout"
            trainingFinished -> "All planned working sets complete"
            else -> "${(plannedSets - completedSets).coerceAtLeast(0)} working set(s) remaining"
        },
        actionCue = when {
            plannedSets == 0 -> "Apply a plan day or add exercises before closeout."
            trainingFinished -> "Mark training complete and move to closeout checks."
            else -> "Complete the remaining working sets before using the session for progression."
        },
        complete = trainingFinished
    )
    val setLogTask = TrainingCloseoutTask(
        label = "Complete set logs",
        status = if (missingLogCount == 0 && completedRows.isNotEmpty()) {
            "Reps, load, and RIR are logged for completed sets"
        } else if (completedRows.isEmpty()) {
            "No completed working sets logged"
        } else {
            "$missingLogCount completed set(s) missing reps, load, or RIR"
        },
        actionCue = if (missingLogCount == 0 && completedRows.isNotEmpty()) {
            "Set data is usable for Exercise History and Progression Cue."
        } else {
            "Fill actual reps, kg, and RIR before AI decides progression."
        },
        complete = missingLogCount == 0 && completedRows.isNotEmpty()
    )
    val riskTask = TrainingCloseoutTask(
        label = "Pain and technique notes",
        status = when {
            hasRiskFlags -> "${dashboard.painFlagCount} pain flag(s), ${dashboard.techniqueFlagCount} technique flag(s)"
            sessionNotesReady -> "Notes present with no major risk flags"
            else -> "No pain, technique, pump, or stimulus note yet"
        },
        actionCue = when {
            hasRiskFlags -> "Do not progress flagged movements until notes and form evidence are reviewed."
            sessionNotesReady -> "Use notes to confirm target-muscle stimulus and movement quality."
            else -> "Add a short note on pump, stimulus, pain, and technique before review."
        },
        complete = !hasRiskFlags && sessionNotesReady
    )
    val photoTask = TrainingCloseoutTask(
        label = "Photo evidence",
        status = when {
            needsPhotoForRisk && needsEquipmentPhoto -> "Form and equipment photos recommended"
            needsPhotoForRisk -> "Form photo recommended"
            needsEquipmentPhoto -> "Equipment photo recommended"
            trainingFormPhotos > 0 || equipmentPhotos > 0 -> "$trainingFormPhotos form photo(s), $equipmentPhotos equipment photo(s)"
            else -> "Optional when setup and technique are clear"
        },
        actionCue = when {
            needsPhotoForRisk -> "Attach a form frame for the painful or unstable movement."
            needsEquipmentPhoto -> "Attach an equipment photo so AI can map it to the visual guide ID."
            trainingFormPhotos > 0 || equipmentPhotos > 0 -> "Photos can be linked with the Unified Exercise Visual Atlas in AI review."
            else -> "Skip if the movement was clear and pain-free; add photos when unsure."
        },
        complete = !needsPhotoForRisk && !needsEquipmentPhoto
    )
    val nutritionTask = TrainingCloseoutTask(
        label = "Post-workout nutrition",
        status = if (hasFoodEvidence) {
            "${log.meals.size} meal(s) or food photo evidence logged"
        } else {
            "No meal or food photo logged"
        },
        actionCue = if (hasFoodEvidence) {
            "AI can compare training output with calories, protein, and carbs."
        } else {
            "Log the post-workout meal or attach a food/label photo before final review."
        },
        complete = hasFoodEvidence
    )
    val metricsTask = TrainingCloseoutTask(
        label = "Metrics and recovery",
        status = when {
            log.metrics.healthSyncedAt.isNotBlank() -> "Health data synced from ${log.metrics.healthDataSource.ifBlank { "Health Connect" }}"
            metricsReady -> "Manual body or recovery metrics present"
            else -> "No body/recovery metric or Health Connect sync today"
        },
        actionCue = if (metricsReady) {
            "AI can compare session output with recovery and body-composition signals."
        } else {
            "Sync Health Connect or log weight, sleep, steps, HR, soreness, fatigue, and stress."
        },
        complete = metricsReady
    )

    val checklist = listOf(completionTask, setLogTask, riskTask, photoTask, nutritionTask, metricsTask)
    val incompleteCount = checklist.count { !it.complete }
    val scorePenalty = incompleteCount * 9 + missingLogCount * 4 + dashboard.painFlagCount * 10 + dashboard.techniqueFlagCount * 5
    val closeoutScore = (96 - scorePenalty).coerceIn(20, 98)
    val primaryActionRoute = when {
        plannedSets == 0 -> DailyExecutionRoute.PLAN
        !trainingFinished || missingLogCount > 0 || !sessionNotesReady || hasRiskFlags -> DailyExecutionRoute.TRAINING
        needsPhotoForRisk || needsEquipmentPhoto -> DailyExecutionRoute.TRAINING
        !hasFoodEvidence -> DailyExecutionRoute.NUTRITION
        !metricsReady -> DailyExecutionRoute.METRICS
        else -> DailyExecutionRoute.AI_REVIEW
    }
    val primaryActionLabel = when (primaryActionRoute) {
        DailyExecutionRoute.PLAN -> "Apply plan"
        DailyExecutionRoute.TRAINING -> when {
            !trainingFinished -> "Finish sets"
            missingLogCount > 0 -> "Complete set logs"
            hasRiskFlags || needsPhotoForRisk || needsEquipmentPhoto -> "Add form evidence"
            else -> "Add session notes"
        }
        DailyExecutionRoute.NUTRITION -> "Log post-workout food"
        DailyExecutionRoute.METRICS -> "Sync recovery metrics"
        DailyExecutionRoute.AI_REVIEW -> if (hasAiReviewToday) "View review" else "Run AI review"
    }
    val statusLabel = when {
        plannedSets == 0 -> "No session loaded"
        !trainingFinished -> "Finish workout"
        missingLogCount > 0 -> "Complete set data"
        hasRiskFlags -> "Risk review needed"
        !hasFoodEvidence -> "Nutrition missing"
        !metricsReady -> "Metrics missing"
        hasAiReviewToday -> "Closeout locked"
        closeoutScore >= 84 -> "Ready for AI review"
        else -> "Review before progressing"
    }
    val photoCue = when {
        needsPhotoForRisk -> "Attach form photos for painful or unstable movements before adding load next time."
        needsEquipmentPhoto -> "Attach equipment photos so the model can match the setup to the Unified Exercise Visual Atlas."
        trainingFormPhotos > 0 || equipmentPhotos > 0 -> "Form/equipment photos are available for visual guide ID and technique review."
        else -> "Photos are optional today; add them when an exercise name, machine setup, or movement path is uncertain."
    }
    val nutritionCue = if (hasFoodEvidence) {
        "Food evidence is available; compare calories, protein, and carbs with the completed training volume."
    } else {
        "Log the recovery meal or attach a food/label photo so the AI can connect training demand with nutrition."
    }
    val metricsCue = if (metricsReady) {
        "Body/recovery metrics are available; use them as trend signals, not one-day absolutes."
    } else {
        "Sync or enter metrics before treating today's performance as a progression signal."
    }
    val sessionNoteCue = if (sessionNotesReady) {
        "Notes are available for pump, stimulus, pain, technique, or substitution context."
    } else {
        "Add a short closeout note: pump, target-muscle feel, technique, pain, energy, and substitutions."
    }
    val aiReviewGate = when {
        hasAiReviewToday -> "Today's AI review is already saved; rerun only after adding new evidence."
        primaryActionRoute == DailyExecutionRoute.AI_REVIEW -> "Ready for a high-confidence daily review."
        else -> "Hold AI progression decisions until the closeout checklist is cleaner."
    }
    val aiReviewFocus = "Use closeout score, completed sets, missing set logs, pain/technique flags, form/equipment photos, post-workout nutrition, metrics sync, Session Quality Dashboard, and Unified Exercise Visual Atlas before changing tomorrow's plan."

    return TrainingCloseoutCoach(
        statusLabel = statusLabel,
        closeoutScore = closeoutScore,
        completedSets = completedSets,
        plannedSets = plannedSets,
        missingLogCount = missingLogCount,
        painFlagCount = dashboard.painFlagCount,
        techniqueFlagCount = dashboard.techniqueFlagCount,
        photoCue = photoCue,
        nutritionCue = nutritionCue,
        metricsCue = metricsCue,
        sessionNoteCue = sessionNoteCue,
        aiReviewGate = aiReviewGate,
        primaryActionLabel = primaryActionLabel,
        primaryActionRoute = primaryActionRoute,
        checklist = checklist,
        aiReviewFocus = aiReviewFocus
    )
}
