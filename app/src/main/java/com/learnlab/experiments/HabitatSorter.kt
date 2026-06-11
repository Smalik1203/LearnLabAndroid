package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.SortBucket
import com.learnlab.engines.SortBuckets
import com.learnlab.engines.SortItem
import com.learnlab.store.ExperimentControls

@Composable
fun HabitatSorter(controls: ExperimentControls) {
    SortBuckets(
        prompt = "Where does each animal actually live? The textbook splits this into three buckets: " +
            "animals that live on land (terrestrial), in water (aquatic), and those that can live in both (amphibians).",
        bucketsTitle = "Habitat",
        buckets = listOf(
            SortBucket("land",  "Terrestrial", "Lives on land."),
            SortBucket("water", "Aquatic",     "Lives in water."),
            SortBucket("both",  "Amphibian",   "Lives in water and on land."),
        ),
        items = listOf(
            SortItem("goat",      "Goat",       "🐐", "land"),
            SortItem("squirrel",  "Squirrel",   "🐿️", "land"),
            SortItem("lion",      "Lion",       "🦁", "land"),
            SortItem("camel",     "Camel",      "🐪", "land"),
            SortItem("seaturtle", "Sea turtle", "🐢", "water"),
            SortItem("whale",     "Whale",      "🐋", "water"),
            SortItem("fish",      "Fish",       "🐟", "water"),
            SortItem("frog",      "Frog",       "🐸", "both"),
        ),
        controls = controls,
    )
}
