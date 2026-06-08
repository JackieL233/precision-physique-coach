package com.iwanttobeanifbbpro.app.core

import com.iwanttobeanifbbpro.app.data.DailyLog

class DailySummaryBuilder {
    fun buildAiReviewContext(log: DailyLog, extraRequest: String = ""): String {
        val totals = log.nutritionTotals()
        val exercises = log.trainingSession.exercises.joinToString("\n") { exercise ->
            "- ${exercise.name}: ${exercise.sets} sets x ${exercise.reps}, ${exercise.loadKg ?: "bodyweight"} kg, RIR ${exercise.rir ?: "unknown"}, target ${exercise.targetMuscle}, notes: ${exercise.notes}"
        }.ifBlank { "- No exercises logged yet." }
        val meals = log.meals.joinToString("\n") { meal ->
            "- ${meal.name}: ${meal.calories} kcal, P ${meal.protein}g, C ${meal.carbs}g, F ${meal.fat}g, fiber ${meal.fiber}g, notes: ${meal.notes}"
        }.ifBlank { "- No meals logged yet." }

        return """
            AI review context for daily training and daily nutrition.

            Date: ${log.date}

            Daily targets:
            - Calories ${log.targets.calories}, protein ${log.targets.protein}g, carbs ${log.targets.carbs}g, fat ${log.targets.fat}g, fiber ${log.targets.fiber}g.

            Daily nutrition logged:
            - Calories ${totals.calories}/${log.targets.calories}
            - Protein ${totals.protein}/${log.targets.protein}g
            - Carbs ${totals.carbs}/${log.targets.carbs}g
            - Fat ${totals.fat}/${log.targets.fat}g
            - Fiber ${totals.fiber}/${log.targets.fiber}g
            Meals:
            $meals

            Daily training:
            - Planned focus: ${log.trainingSession.plannedFocus}
            - Completed: ${log.trainingSession.completed}
            - Session notes: ${log.trainingSession.sessionNotes}
            - Exercise log:
            $exercises

            Daily metrics:
            - bodyWeightKg: ${log.metrics.bodyWeightKg ?: "not logged"}
            - waistCm: ${log.metrics.waistCm ?: "not logged"}
            - sleepHours: ${log.metrics.sleepHours ?: "not logged"}
            - steps: ${log.metrics.steps}
            - hunger: ${log.metrics.hunger}/5
            - fatigue: ${log.metrics.fatigue}/5
            - soreness: ${log.metrics.soreness}/5
            - stress: ${log.metrics.stress}/5

            User reflection:
            ${log.reflection.ifBlank { "No reflection logged." }}

            Extra request:
            ${extraRequest.ifBlank { "Perform an AI review and update recommendations for tomorrow." }}

            Please perform an AI review: identify the limiting factor, compare training execution with nutrition adherence, recommend the smallest useful adjustment, and specify what to track tomorrow.
        """.trimIndent()
    }
}
