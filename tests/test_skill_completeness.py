import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SKILL = ROOT / "skills" / "precision-physique-coach"


def read(relative: str) -> str:
    return (SKILL / relative).read_text(encoding="utf-8").lower()


class SkillCompletenessTest(unittest.TestCase):
    def test_chinese_readme_covers_core_usage(self) -> None:
        path = ROOT / "README.zh-CN.md"
        self.assertTrue(path.exists(), "README.zh-CN.md")
        text = path.read_text(encoding="utf-8")
        expected_terms = [
            "Precision Physique Coach",
            "$precision-physique-coach",
            "安装",
            "训练计划",
            "现有计划",
            "训练日志",
            "容量",
            "安全",
            "营养",
            "恢复",
        ]
        for term in expected_terms:
            with self.subTest(term=term):
                self.assertIn(term, text)

    def test_required_expert_references_exist_and_are_linked_from_skill(self) -> None:
        skill_text = read("SKILL.md")
        required = [
            "references/intake-assessment.md",
            "references/anatomy-and-movement.md",
            "references/goal-decision-system.md",
            "references/phase-templates.md",
            "references/adaptation-playbook.md",
            "references/plan-optimization.md",
            "references/session-execution-and-volume.md",
            "references/model-adaptation.md",
        ]
        for relative in required:
            with self.subTest(relative=relative):
                self.assertTrue((SKILL / relative).exists(), relative)
                self.assertIn(relative, skill_text)

    def test_templates_cover_intake_checkin_and_plan_outputs(self) -> None:
        required = [
            "assets/templates/intake-form.md",
            "assets/templates/check-in-form.md",
            "assets/templates/plan-template.md",
            "assets/templates/session-log.csv",
            "assets/templates/tracking-log.csv",
        ]
        for relative in required:
            with self.subTest(relative=relative):
                path = SKILL / relative
                self.assertTrue(path.exists(), relative)
                text = path.read_text(encoding="utf-8").lower()
                self.assertTrue("safety" in text or "red flag" in text or "pain" in text)
                self.assertIn("goal", text)

    def test_training_references_cover_major_professional_dimensions(self) -> None:
        combined = "\n".join(
            (SKILL / "references" / name).read_text(encoding="utf-8").lower()
            for name in [
                "training-programming.md",
                "exercise-library.md",
                "anatomy-and-movement.md",
                "phase-templates.md",
                "adaptation-playbook.md",
            ]
        )
        expected_terms = [
            "mesocycle",
            "rir",
            "movement pattern",
            "hypertrophy",
            "fat loss",
            "recomposition",
            "deload",
            "weak point",
            "joint",
            "substitution",
        ]
        for term in expected_terms:
            with self.subTest(term=term):
                self.assertIn(term, combined)

    def test_scripts_include_target_estimation_and_checkin_analysis(self) -> None:
        scripts = SKILL / "scripts"
        self.assertTrue((scripts / "estimate_targets.py").exists())
        self.assertTrue((scripts / "analyze_checkin.py").exists())
        self.assertTrue((scripts / "analyze_training_volume.py").exists())

    def test_execution_and_volume_references_cover_plan_optimization(self) -> None:
        combined = "\n".join(
            (SKILL / "references" / name).read_text(encoding="utf-8").lower()
            for name in [
                "plan-optimization.md",
                "session-execution-and-volume.md",
                "data-tracking-adjustment.md",
                "adaptation-playbook.md",
            ]
        )
        expected_terms = [
            "existing plan",
            "audit",
            "session log",
            "hard sets",
            "tonnage",
            "effective reps",
            "volume landmarks",
            "training quality",
            "progression decision",
            "exercise-level",
        ]
        for term in expected_terms:
            with self.subTest(term=term):
                self.assertIn(term, combined)


if __name__ == "__main__":
    unittest.main()
