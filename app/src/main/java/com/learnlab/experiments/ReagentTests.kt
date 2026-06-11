package com.learnlab.experiments

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.learnlab.engines.LabFood
import com.learnlab.engines.LabReagentScreen
import com.learnlab.engines.ReagentConfig
import com.learnlab.store.ExperimentControls

@Composable
fun IodineStarchTest(controls: ExperimentControls) {
    LabReagentScreen(
        foods = listOf(
            LabFood("potato",   "Potato",          "🥔", true,  "Rich in starch — iodine binds and turns blue-black."),
            LabFood("rice",     "Boiled rice",     "🍚", true,  "Cereals like rice are mostly starch."),
            LabFood("bread",    "Bread",           "🍞", true,  "Made from wheat flour — packed with starch."),
            LabFood("gram",     "Boiled chana",    "🫘", true,  "Chickpeas contain starch alongside their protein."),
            LabFood("cucumber", "Cucumber",        "🥒", false, "Mostly water and fibre. No starch to react with iodine."),
            LabFood("peanut",   "Crushed peanuts", "🥜", false, "Peanuts are mostly fat and protein — not starch."),
            LabFood("butter",   "Butter",          "🧈", false, "Butter is fat. No starch present."),
            LabFood("sugar",    "Sugar",           "🍬", false, "Sugar IS a carbohydrate — but iodine only reacts with starch, not simple sugars. A great puzzle."),
        ),
        reagent = ReagentConfig(
            prompt = "Pick a food. Predict whether iodine will turn it blue-black. Then drop iodine and see what happens.",
            reagentName = "iodine solution",
            actionLabel = "Drop iodine",
            positiveLabel = "Blue-black — starch present",
            negativeLabel = "No change",
            positiveColor = Color(0xFF0F172A),
            negativeColor = Color(0xFFD97706),
            positiveBadge = "Starch",
            negativeBadge = "None",
        ),
        controls = controls,
    )
}

@Composable
fun FatPaperTest(controls: ExperimentControls) {
    LabReagentScreen(
        foods = listOf(
            LabFood("butter",   "Butter",          "🧈", true,  "Almost pure fat — paper goes clearly translucent."),
            LabFood("peanut",   "Crushed peanuts", "🥜", true,  "Peanuts are rich in oil — leaves a strong oily patch."),
            LabFood("coconut",  "Coconut",         "🥥", true,  "Coconut flesh contains significant fat."),
            LabFood("oil",      "Cooking oil",     "🫒", true,  "Just fat. The patch is immediate and obvious."),
            LabFood("rice",     "Boiled rice",     "🍚", false, "Almost all starch — no oily patch."),
            LabFood("potato",   "Potato",          "🥔", false, "Mostly starch and water. The paper may wet a little but won't go translucent."),
            LabFood("cucumber", "Cucumber",        "🥒", false, "Mostly water and fibre, no fat."),
            LabFood("sugar",    "Sugar",           "🍬", false, "Pure carbohydrate — no fat at all."),
        ),
        reagent = ReagentConfig(
            prompt = "Press each food onto paper, let it dry, then hold it up to the light. A translucent patch means fat is present.",
            reagentName = "paper test",
            actionLabel = "Press onto paper",
            positiveLabel = "Oily patch — fat present",
            negativeLabel = "No oily patch",
            positiveColor = Color(0xFFEAB308),
            negativeColor = Color(0xFF334155),
            positiveBadge = "Fat",
            negativeBadge = "None",
            isFatPaperTest = true,
        ),
        controls = controls,
    )
}

@Composable
fun ProteinVioletTest(controls: ExperimentControls) {
    LabReagentScreen(
        foods = listOf(
            LabFood("soya",    "Soya bean",        "🫘", true,  "Excellent plant protein — turns clearly violet."),
            LabFood("paneer",  "Paneer",           "🧀", true,  "Milk protein — strong violet reaction."),
            LabFood("egg",     "Egg white",        "🥚", true,  "Egg whites are almost pure protein."),
            LabFood("peanut",  "Crushed peanuts",  "🥜", true,  "Peanuts have protein AND fat — both tests would show positive."),
            LabFood("pea",     "Peas",             "🟢", true,  "Legumes are reliable protein sources."),
            LabFood("rice",    "Boiled rice",      "🍚", false, "Mostly starch — very little protein."),
            LabFood("sugar",   "Sugar",            "🍬", false, "Pure carbohydrate. No protein at all."),
            LabFood("butter",  "Butter",           "🧈", false, "Fat, almost no protein."),
        ),
        reagent = ReagentConfig(
            prompt = "Mash the food, add water, then 2 drops of copper sulfate and 10 drops of caustic soda. Shake. A violet colour means proteins are present.",
            reagentName = "copper sulfate + caustic soda",
            actionLabel = "Add reagents",
            positiveLabel = "Violet — protein present",
            negativeLabel = "No colour change",
            positiveColor = Color(0xFF5B21B6),
            negativeColor = Color(0xFFCBD5E1),
            positiveBadge = "Protein",
            negativeBadge = "None",
            reagentDropColor = Color(0xFF3B82F6),
        ),
        controls = controls,
    )
}
