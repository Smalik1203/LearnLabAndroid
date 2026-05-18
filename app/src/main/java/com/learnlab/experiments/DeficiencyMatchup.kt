package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.MatchRow
import com.learnlab.engines.MatchTile
import com.learnlab.engines.ThreeColumnMatch
import com.learnlab.store.ExperimentControls

@Composable
fun DeficiencyMatchup(controls: ExperimentControls) {
    ThreeColumnMatch(
        prompt = "A patient walks in with a symptom. Trace the chain: which nutrient is missing, and which food brings it back? " +
            "Pick a symptom, then the matching nutrient, then the food.",
        columnTitles = Triple("Symptom", "Missing nutrient", "Best food source"),
        rows = listOf(
            MatchRow("scurvy",
                MatchTile("Bleeding gums, slow wound healing", "🦷"),
                MatchTile("Vitamin C", "🍋"),
                MatchTile("Amla, orange, guava", "🍊"),
                "Scurvy. Vitamin C from citrus and amla restores wound healing and gum health."),
            MatchRow("rickets",
                MatchTile("Soft, bending bones in a child", "🦴"),
                MatchTile("Vitamin D", "☀️"),
                MatchTile("Sunlight, milk, fish, eggs", "🥛"),
                "Rickets. Vitamin D (from sunlight + foods) helps the body absorb calcium for hard bones."),
            MatchRow("goitre",
                MatchTile("Swelling at the front of the neck", "🧣"),
                MatchTile("Iodine", "🧂"),
                MatchTile("Iodised salt, seaweed", "🌊"),
                "Goitre. Iodised salt was introduced in India to fix this in regions with iodine-poor soil."),
            MatchRow("anaemia",
                MatchTile("Weakness, shortness of breath", "😮‍💨"),
                MatchTile("Iron", "⚙️"),
                MatchTile("Spinach, beetroot, pomegranate", "🥬"),
                "Anaemia. Iron is needed to make haemoglobin in red blood cells."),
            MatchRow("night-blindness",
                MatchTile("Trouble seeing in dim light", "🌒"),
                MatchTile("Vitamin A", "🥕"),
                MatchTile("Carrot, papaya, mango, milk", "🥭"),
                "Night blindness — first sign of Vitamin A deficiency. Carotenoid-rich foods reverse early symptoms."),
            MatchRow("beriberi",
                MatchTile("Tingling feet, breathing trouble", "🦶"),
                MatchTile("Vitamin B1", "🌾"),
                MatchTile("Whole grains, legumes, nuts", "🥜"),
                "Beriberi. Refined cereals lose B1; whole grains and pulses restore it."),
        ),
        controls = controls,
    )
}
