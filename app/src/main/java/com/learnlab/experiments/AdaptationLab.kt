package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.MatchRow
import com.learnlab.engines.MatchTile
import com.learnlab.engines.ThreeColumnMatch
import com.learnlab.store.ExperimentControls

@Composable
fun AdaptationLab(controls: ExperimentControls) {
    ThreeColumnMatch(
        prompt = "Every plant or animal in the textbook has a feature that fits its habitat. " +
            "Build the chain: pick the organism, then the feature it has, then the habitat reason it helps with.",
        columnTitles = Triple("Organism", "Special feature", "Why it helps"),
        rows = listOf(
            MatchRow(
                id = "hot-camel",
                a = MatchTile("Camel of hot desert (Rajasthan)", "🐪"),
                b = MatchTile("Long legs, wide hooves, one hump", "🦵"),
                c = MatchTile("Walks on loose sand without sinking; hump stores food.", "🏜️"),
                note = "Hot-desert camel: tall legs + broad hooves + one hump for food storage in scarce conditions.",
            ),
            MatchRow(
                id = "cold-camel",
                a = MatchTile("Camel of cold desert (Ladakh)", "🐫"),
                b = MatchTile("Short legs, two humps, long hair", "🧥"),
                c = MatchTile("Walks rocky mountain ground; long hair survives freezing winter.", "❄️"),
                note = "Cold-desert camel: shorter, two humps that shrink in winter, long fur for sub-zero nights.",
            ),
            MatchRow(
                id = "cactus",
                a = MatchTile("Cactus in Rajasthan desert", "🌵"),
                b = MatchTile("Thick fleshy stem, almost no leaves", "🟢"),
                c = MatchTile("Stores water; tiny leaves lose less water.", "💧"),
                note = "Cactus: water stored in the fleshy stem, leaves reduced to spines to cut water loss.",
            ),
            MatchRow(
                id = "deodar",
                a = MatchTile("Deodar tree in Himalayas", "🌲"),
                b = MatchTile("Conical shape, sloping branches", "📐"),
                c = MatchTile("Snow slides off; branches don't break under weight.", "🏔️"),
                note = "Deodar: conical silhouette = snow-shedding architecture for high-altitude winters.",
            ),
            MatchRow(
                id = "fish",
                a = MatchTile("Fish", "🐟"),
                b = MatchTile("Streamlined body and fins", "🌀"),
                c = MatchTile("Cuts through water with minimal resistance.", "🌊"),
                note = "Fish: a body shape and fins evolved for aquatic locomotion.",
            ),
            MatchRow(
                id = "rhody",
                a = MatchTile("Mountain-top rhododendron", "🌸"),
                b = MatchTile("Short height, small leaves", "🌱"),
                c = MatchTile("Survives heavy winds at altitude.", "🌬️"),
                note = "Same species can grow tall in sheltered valleys, short on windy mountain tops.",
            ),
        ),
        controls = controls,
    )
}
