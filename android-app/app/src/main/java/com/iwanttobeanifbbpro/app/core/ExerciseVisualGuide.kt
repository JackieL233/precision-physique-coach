package com.iwanttobeanifbbpro.app.core

import java.util.Locale

enum class ExerciseVisualType {
    SMITH_MACHINE,
    CABLE,
    DUMBBELL,
    BARBELL,
    MACHINE,
    BENCH,
    PULL_UP_STATION,
    BAND,
    LEG_PRESS,
    BODYWEIGHT
}

data class ExerciseVisualSpec(
    val type: ExerciseVisualType,
    val equipment: String,
    val pattern: String,
    val primaryMuscle: String,
    val setupCue: String,
    val example: String,
    val lookFor: String
) {
    fun visualPromptLine(): String {
        return "Exercise visual guide: $equipment | $pattern | target $primaryMuscle | setup cue: $setupCue | example movement: $example | look-for cue: $lookFor"
    }
}

fun exerciseVisualLibrarySpecs(): List<ExerciseVisualSpec> {
    return listOf(
        exerciseVisualSpec("Smith incline press", "Upper chest"),
        exerciseVisualSpec("Cable lateral raise", "Side delt"),
        exerciseVisualSpec("Dumbbell row", "Back"),
        exerciseVisualSpec("Barbell squat", "Quads"),
        exerciseVisualSpec("Machine chest press", "Chest"),
        exerciseVisualSpec("Incline dumbbell press bench setup", "Upper chest"),
        exerciseVisualSpec("Pull-up", "Lats"),
        exerciseVisualSpec("Band face pull", "Rear delts"),
        exerciseVisualSpec("Leg press", "Quads"),
        exerciseVisualSpec("Push-up", "Chest")
    )
}

fun exerciseVisualSpec(name: String, targetMuscle: String): ExerciseVisualSpec {
    val text = "$name $targetMuscle".lowercase(Locale.US)
    val primaryMuscle = targetMuscle.ifBlank { inferPrimaryMuscle(text) }
    return when {
        text.contains("smith") -> ExerciseVisualSpec(
            type = ExerciseVisualType.SMITH_MACHINE,
            equipment = "Smith machine",
            pattern = "Guided fixed-bar path",
            primaryMuscle = primaryMuscle,
            setupCue = "Align bench or stance under the fixed bar path",
            example = "Incline Smith Press",
            lookFor = "Look for two rails and a fixed bar"
        )

        text.contains("cable") ||
            text.contains("pulley") ||
            text.contains("rope") ||
            text.contains("pushdown") ||
            text.contains("face pull") ||
            text.contains("pulldown") -> ExerciseVisualSpec(
            type = ExerciseVisualType.CABLE,
            equipment = "Cable station",
            pattern = "Pulley resistance",
            primaryMuscle = primaryMuscle,
            setupCue = "Set pulley height before choosing handle",
            example = "Cable Lateral Raise",
            lookFor = "Look for a cable, pulley, and handle"
        )

        text.contains("leg press") ||
            text.contains("hack squat") ||
            text.contains("pendulum") -> ExerciseVisualSpec(
            type = ExerciseVisualType.LEG_PRESS,
            equipment = "Leg press or hack squat",
            pattern = "Guided lower-body sled path",
            primaryMuscle = primaryMuscle,
            setupCue = "Set seat, foot position, safety stops, and controlled depth",
            example = "Leg Press",
            lookFor = "Look for a sled platform, seat, and safety handles"
        )

        text.contains("pull-up") ||
            text.contains("pullup") ||
            text.contains("chin-up") ||
            text.contains("chinup") ||
            text.contains("dip") -> ExerciseVisualSpec(
            type = ExerciseVisualType.PULL_UP_STATION,
            equipment = "Pull-up/Dip station",
            pattern = "Bodyweight vertical support",
            primaryMuscle = primaryMuscle,
            setupCue = "Choose grip, brace, and use assistance if reps break down",
            example = "Pull-up",
            lookFor = "Look for an overhead bar, dip handles, or assist platform"
        )

        text.contains("band") -> ExerciseVisualSpec(
            type = ExerciseVisualType.BAND,
            equipment = "Resistance band",
            pattern = "Elastic resistance",
            primaryMuscle = primaryMuscle,
            setupCue = "Anchor the band securely and control the end range",
            example = "Band Face Pull",
            lookFor = "Look for a band anchored to a rack, door, or post"
        )

        text.contains("dumbbell") ||
            text.contains("db ") ||
            text.endsWith(" db") -> ExerciseVisualSpec(
            type = ExerciseVisualType.DUMBBELL,
            equipment = "Dumbbells",
            pattern = "Free-weight unilateral control",
            primaryMuscle = primaryMuscle,
            setupCue = "Match both sides and control the path",
            example = "Dumbbell Row",
            lookFor = "Look for one or two handheld weights"
        )

        text.contains("barbell") ||
            text.contains("bench press") ||
            text.contains("deadlift") ||
            text.contains("squat") -> ExerciseVisualSpec(
            type = ExerciseVisualType.BARBELL,
            equipment = "Barbell",
            pattern = "Free-weight compound lift",
            primaryMuscle = primaryMuscle,
            setupCue = "Set rack height, grip, stance, and safety pins",
            example = "Barbell Squat",
            lookFor = "Look for a straight bar, plates, and rack"
        )

        text.contains("bench") ||
            text.contains("hip thrust") ||
            text.contains("step-up") ||
            text.contains("step up") ||
            text.contains("bulgarian") -> ExerciseVisualSpec(
            type = ExerciseVisualType.BENCH,
            equipment = "Adjustable bench",
            pattern = "Bench-supported movement",
            primaryMuscle = primaryMuscle,
            setupCue = "Set bench angle and body contact before loading",
            example = "Incline Dumbbell Press",
            lookFor = "Look for a flat or adjustable bench"
        )

        text.contains("machine") ||
            text.contains("press") ||
            text.contains("extension") ||
            text.contains("curl") ||
            text.contains("pec deck") ||
            text.contains("leg") -> ExerciseVisualSpec(
            type = ExerciseVisualType.MACHINE,
            equipment = "Machine",
            pattern = "Guided resistance",
            primaryMuscle = primaryMuscle,
            setupCue = "Adjust seat and pads to match joint axis",
            example = "Machine Chest Press",
            lookFor = "Look for a seat, pads, handles, or lever arms"
        )

        else -> ExerciseVisualSpec(
            type = ExerciseVisualType.BODYWEIGHT,
            equipment = "Bodyweight or open station",
            pattern = "Movement pattern demo",
            primaryMuscle = primaryMuscle,
            setupCue = "Use notes or photos when the exact setup is unclear",
            example = "Push-up",
            lookFor = "Look for floor, bench, bar, bands, or open space"
        )
    }
}

private fun inferPrimaryMuscle(text: String): String {
    return when {
        text.contains("lateral") || text.contains("delt") || text.contains("shoulder") -> "Delts"
        text.contains("incline") || text.contains("chest") || text.contains("press") || text.contains("pec") -> "Chest"
        text.contains("row") || text.contains("pulldown") || text.contains("lat") || text.contains("back") -> "Back"
        text.contains("squat") || text.contains("leg press") || text.contains("quad") -> "Quads"
        text.contains("deadlift") || text.contains("rdl") || text.contains("hamstring") || text.contains("glute") -> "Posterior chain"
        text.contains("curl") || text.contains("biceps") -> "Biceps"
        text.contains("extension") || text.contains("pushdown") || text.contains("triceps") -> "Triceps"
        text.contains("calf") -> "Calves"
        else -> "Target muscle"
    }
}
