# Precision Physique Coach Skill 中文说明

Precision Physique Coach 是一个开源 AI skill，用于个人精细化体态与健身训练指导。它可以帮助用户创建新的训练计划，也可以审查和优化现有计划，并结合训练日志、身体数据、饮食数据、维度数据、恢复状态和训练质量持续调整。

这个项目不是医疗、物理治疗或注册营养治疗工具。出现胸痛、晕厥、异常呼吸困难、急性损伤、持续疼痛、进食障碍风险、孕期/产后复杂情况或其他红旗信号时，应先寻求合格专业人士帮助。

## 核心能力

- 创建个性化训练计划：增肌、减脂、重组、维持、恢复训练、弱项强化、阶段化训练。
- 优化现有计划：检查训练目标是否匹配、肌群容量是否合理、动作是否冗余、疲劳是否过高、进阶规则是否清晰。
- 细化每次训练：给出具体动作、目标肌群、组数、次数、RIR/RPE、休息时间、技术提示、替代动作和记录方式。
- 追踪训练日志：记录每组的负重、次数、RIR/RPE、技术质量、目标肌群刺激感、疼痛、完成情况和备注。
- 分析训练容量：汇总 hard sets、tonnage、effective reps、动作表现、肌群刺激、疼痛标记和训练质量。
- 指导营养：估算热量、蛋白质、脂肪、碳水、纤维、水分和根据体重趋势调整。
- 管理恢复：睡眠、疲劳、酸痛、疼痛规则、deload、训练压力和生活压力。
- 数据化调整：根据体重均值、围度、照片、训练表现、训练日志、饮食执行率和恢复指标调整计划。

## 安装

把 skill 文件夹复制到 Codex skills 目录：

```bash
cp -R skills/precision-physique-coach ~/.codex/skills/
```

然后可以这样调用：

```text
Use $precision-physique-coach to create a 12-week hypertrophy plan for me.
```

也可以用中文直接说明你的目标：

```text
Use $precision-physique-coach。请根据我的身体数据、训练经验、器械条件和时间安排，创建一个 12 周增肌训练计划，并给出饮食和每周追踪方式。
```

## 常见使用方式

创建新计划：

```text
Use $precision-physique-coach。我每周可以训练 4 天，目标是增肌，健身房器械齐全，请给我一个阶段化训练计划。
```

优化现有计划：

```text
Use $precision-physique-coach。下面是我现在的 push/pull/legs 计划，请帮我检查每个肌群的容量、动作顺序、弱项覆盖和进阶规则。
```

分析训练日志：

```text
Use $precision-physique-coach。请根据我的 session-log.csv 分析本周训练质量、各肌群 hard sets、tonnage、疼痛风险，并告诉我下周哪些动作加重量、加次数、维持、减量或替换。
```

减脂平台调整：

```text
Use $precision-physique-coach。我连续两周体重均值没变，但腰围下降 1 cm，训练表现稳定，饮食执行率 90%。我需要降低热量吗？
```

## 目录结构

```text
skills/precision-physique-coach/
  SKILL.md
  agents/openai.yaml
  references/
    safety-screening.md
    intake-assessment.md
    anatomy-and-movement.md
    goal-decision-system.md
    plan-optimization.md
    training-programming.md
    exercise-library.md
    phase-templates.md
    session-execution-and-volume.md
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
  scripts/
    estimate_targets.py
    analyze_checkin.py
    analyze_training_volume.py
```

## 关键模板

- `intake-form.md`：初次咨询和安全筛查。
- `plan-template.md`：完整训练计划输出模板。
- `check-in-form.md`：每周或每两周反馈表。
- `session-log.csv`：每次训练、每个动作、每组训练的详细记录。
- `tracking-log.csv`：身体、饮食、恢复和执行率长期追踪。

## 脚本

估算热量和宏量营养：

```bash
python skills/precision-physique-coach/scripts/estimate_targets.py --sex male --age 30 --height-cm 178 --weight-kg 80 --activity moderate --goal fat-loss
```

分析 check-in 趋势：

```bash
python skills/precision-physique-coach/scripts/analyze_checkin.py --input-json examples/sample-checkin.json
```

分析训练容量和训练质量：

```bash
python skills/precision-physique-coach/scripts/analyze_training_volume.py --session-csv examples/sample-session-log.csv
```

## 安全边界

这个 skill 会优先做安全筛查。它可以帮助你组织训练、营养、恢复和数据追踪，但不能诊断疾病、治疗伤病、替代医生、物理治疗师或注册营养师。任何疼痛加重、动作变形、急性损伤、胸痛、晕厥、神经症状、极端节食或进食障碍风险，都应该暂停相关建议并寻求专业帮助。

## 验证

运行：

```bash
python -m unittest tests.test_skill_completeness tests.test_scripts_smoke
python scripts/validate_skill.py skills/precision-physique-coach
```

## GitHub

仓库地址：

https://github.com/JackieL233/precision-physique-coach
