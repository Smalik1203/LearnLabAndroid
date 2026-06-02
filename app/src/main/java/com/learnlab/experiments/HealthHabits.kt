package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.MatchRow
import com.learnlab.engines.MatchTile
import com.learnlab.engines.ThreeColumnMatch
import com.learnlab.store.ExperimentControls

@Composable
fun HealthHabits(controls: ExperimentControls) {
    ThreeColumnMatch(
        prompt = "Match each daily habit to how it works in your body, " +
            "then to what it builds or breaks over time.",
        columnTitles = Triple("Habit", "Mechanism", "Outcome"),
        rows = listOf(
            MatchRow(
                id = "exercise",
                a = MatchTile("Daily exercise", "🏃"),
                b = MatchTile("Strengthens heart muscle", "💓"),
                c = MatchTile("Lower blood pressure", "🩺"),
                note = "Exercise repeatedly contracts the heart — over months the muscle " +
                    "grows stronger, dropping resting blood pressure.",
            ),
            MatchRow(
                id = "sleep",
                a = MatchTile("8 hours of sleep", "😴"),
                b = MatchTile("Brain clears waste at night", "🧠"),
                c = MatchTile("Sharp memory and immunity", "🧬"),
                note = "Deep sleep activates the glymphatic system to flush brain waste, " +
                    "while growth hormone repairs the body and resets immunity.",
            ),
            MatchRow(
                id = "sunlight",
                a = MatchTile("Outdoor sunlight", "☀️"),
                b = MatchTile("Skin makes vitamin D", "🦴"),
                c = MatchTile("Strong bones and better mood", "🌞"),
                note = "UV-B converts a cholesterol precursor in the skin into vitamin D, " +
                    "which fixes calcium into bone; daylight also lifts serotonin.",
            ),
            MatchRow(
                id = "tobacco",
                a = MatchTile("Tobacco use", "🚬"),
                b = MatchTile("Tar damages air sacs", "🫁"),
                c = MatchTile("Lung disease and COPD", "⚠️"),
                note = "Tar deposits permanently destroy the alveoli — the tiny sacs where " +
                    "oxygen enters the blood — leading to emphysema and COPD.",
            ),
            MatchRow(
                id = "sugary",
                a = MatchTile("Sugary drinks", "🥤"),
                b = MatchTile("Repeated insulin spikes", "📈"),
                c = MatchTile("Type 2 diabetes risk", "🩸"),
                note = "Every sugary drink jolts insulin; years of spikes wear out the " +
                    "response and cells stop listening — that is type 2 diabetes.",
            ),
            MatchRow(
                id = "sedentary",
                a = MatchTile("Sedentary lifestyle", "🪑"),
                b = MatchTile("Muscle protein breaks down", "💪"),
                c = MatchTile("Atrophy and obesity", "⚖️"),
                note = "Muscles need mechanical load to maintain protein. Sitting also " +
                    "drops lipoprotein lipase by 90% within an hour — blood fats stop clearing.",
            ),
        ),
        controls = controls,
    )
}
