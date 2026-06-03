# Pro Card Physique Coach Skill

[????](README.zh-CN.md)

An open-source AI skill for serious physique coaching and long-term bodybuilding development: training plans, existing-plan audits, improvement seasons, contest prep, posing, nutrition, recovery, injury-risk controls, workout logs, and data-driven check-ins for users who may aspire to an IFBB Pro / pro card level goal.

This project is not affiliated with, endorsed by, or an official resource of the IFBB Pro League, NPC, or NPC Worldwide. Always verify current qualification paths, divisions, rules, event details, and pro-card availability with official organizers.

The canonical skill lives in:

`skills/pro-card-physique-coach/`

## What It Does

- Builds individualized bodybuilding and fitness plans from goals, body data, equipment, schedule, training age, division target, and constraints.
- Audits and optimizes existing plans instead of blindly replacing them.
- Supports hypertrophy, improvement season, fat loss, recomposition, maintenance, deloads, contest prep, and return-to-training.
- Helps map a long-term IFBB Pro / pro card aspiration into seasons, weak-point priorities, readiness criteria, and official-rule checkpoints.
- Creates periodized training plans with exercises, sets, reps, RIR/RPE, progression, deloads, cardio, and substitutions.
- Tracks each workout at exercise/set level, including hard sets, tonnage, RIR/RPE, pain, technique quality, and target-muscle stimulus.
- Guides calories/macros, meal timing, hydration, supplements, and adjustment rules within conservative safety boundaries.
- Covers posing practice, stage conditioning, show logistics, peak-week caution, and post-show recovery.
- Uses safety screening and referral boundaries instead of pretending to be a doctor.

## Repository Structure

```text
skills/pro-card-physique-coach/
  SKILL.md
  agents/openai.yaml
  references/
    safety-screening.md
    intake-assessment.md
    anatomy-and-movement.md
    goal-decision-system.md
    pro-card-roadmap.md
    plan-optimization.md
    training-programming.md
    exercise-library.md
    phase-templates.md
    session-execution-and-volume.md
    contest-prep-and-posing.md
    nutrition-body-composition.md
    recovery-injury-risk.md
    data-tracking-adjustment.md
    adaptation-playbook.md
    model-adaptation.md
    sources.md
  assets/templates/
    intake-form.md
    check-in-form.md
    plan-template.md
    session-log.csv
    tracking-log.csv
  scripts/estimate_targets.py
  scripts/analyze_checkin.py
  scripts/analyze_training_volume.py
```

## Install for Codex

Copy or symlink the skill folder into your Codex skills directory:

```bash
cp -R skills/pro-card-physique-coach ~/.codex/skills/
```

Then invoke it with:

```text
Use $pro-card-physique-coach to create a long-term bodybuilding roadmap toward a pro card goal.
```

## Use With Other Models

Use `SKILL.md` as the main system/developer instruction. Keep the `references/` files available for retrieval, tool loading, or manual context injection when the model needs detail.

Recommended runtime behavior:

1. Load `SKILL.md` first.
2. Load only the relevant reference files for the user's request.
3. Keep the plan grounded in user data, official-rule verification, and safety boundaries.
4. Iterate from tracked trends rather than one-off measurements.

## Example Prompts

```text
Use $pro-card-physique-coach to build a 2-year roadmap from intermediate lifter to regional bodybuilding competitor, with weak-point priorities and check-in metrics.
```

```text
Use $pro-card-physique-coach to audit my current push/pull/legs plan and tell me how to adjust weekly hard sets, exercise order, and progression for Classic Physique.
```

```text
Use $pro-card-physique-coach to analyze this session log and decide which exercises should add reps, hold, deload, or be swapped.
```

```text
Use $pro-card-physique-coach to plan a conservative contest-prep outline, posing schedule, and safety checkpoints for a show I am considering.
```

See [examples/pressure-scenarios.md](examples/pressure-scenarios.md) for behavior tests and realistic usage scenarios.

## Safety

This skill is for coaching support and education. It is not medical care, physical therapy, dietetic treatment, drug guidance, or an official contest-rules source. Users with symptoms, injuries, medical conditions, pregnancy/postpartum needs, eating-disorder risk, extreme body-composition goals, or drug/diuretic questions should work with qualified professionals and official organizers.

## Evidence-Informed Sources

The references are summarized from widely used public guidance and official/public anchors, including:

- WHO 2020 guidelines on physical activity and sedentary behavior.
- ACSM 2026 position stand on resistance-training prescription for healthy adults.
- ACSM exercise screening and sports nutrition position statements.
- International Society of Sports Nutrition position stands on protein, diets/body composition, and creatine.
- Official IFBB Pro League / NPC / NPC Worldwide public resources for rule-verification prompts and pathway awareness.

See [references/sources.md](skills/pro-card-physique-coach/references/sources.md) for source links.

## License

MIT
