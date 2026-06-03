# Adaptive Bodybuilding Coach Skill

An open-source AI skill for personalized physique coaching: hypertrophy, fat loss, recomposition, nutrition targets, gym exercise selection, recovery, injury-risk controls, and data-driven check-ins.

The project is designed as a portable skill that can be adapted for Codex, ChatGPT-style skill systems, Claude projects, Gemini Gems, custom agent prompts, or other LLM runtimes. The canonical skill lives in:

`skills/adaptive-bodybuilding-coach/`

## What It Does

- Builds individualized bodybuilding and fitness plans from goals, body data, equipment, schedule, training age, and constraints.
- Supports hypertrophy, fat loss, lean gaining, recomposition, maintenance, deloads, and return-to-training.
- Creates periodized training plans with exercises, sets, reps, RIR/RPE, progression, deloads, cardio, and substitutions.
- Guides calorie and macro targets, meal timing, hydration, supplements, and adjustment rules.
- Tracks body weight, measurements, photos, training performance, adherence, sleep, hunger, soreness, and fatigue.
- Uses safety screening and referral boundaries instead of pretending to be a doctor.

## Repository Structure

```text
skills/adaptive-bodybuilding-coach/
  SKILL.md
  agents/openai.yaml
  references/
    safety-screening.md
    training-programming.md
    exercise-library.md
    nutrition-body-composition.md
    recovery-injury-risk.md
    data-tracking-adjustment.md
  assets/templates/plan-template.md
  scripts/estimate_targets.py
```

## Install for Codex

Copy or symlink the skill folder into your Codex skills directory:

```bash
cp -R skills/adaptive-bodybuilding-coach ~/.codex/skills/
```

Then invoke it with:

```text
Use $adaptive-bodybuilding-coach to create a 12-week hypertrophy plan for me.
```

## Use With Other Models

Use `SKILL.md` as the main system/developer instruction. Keep the `references/` files available for retrieval, tool loading, or manual context injection when the model needs detail.

Recommended runtime behavior:

1. Load `SKILL.md` first.
2. Load only the relevant reference files for the user's request.
3. Keep the plan grounded in user data and safety boundaries.
4. Iterate from tracked trends rather than one-off measurements.

## Safety

This skill is for coaching support and education. It is not medical care, physical therapy, or dietetic treatment. Users with symptoms, injuries, medical conditions, pregnancy/postpartum needs, eating-disorder risk, or extreme body-composition goals should work with qualified professionals.

## Evidence-Informed Sources

The references are summarized from widely used public guidance and consensus positions, including:

- WHO 2020 guidelines on physical activity and sedentary behavior.
- ACSM 2026 position stand on resistance-training prescription for healthy adults.
- ACSM exercise screening and sports nutrition position statements.
- International Society of Sports Nutrition position stands on protein, diets/body composition, and creatine.

See [references/sources.md](skills/adaptive-bodybuilding-coach/references/sources.md) for source links.

## License

MIT
