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
            SortItem("horse",     "Horse",     "🐴", "land"),
            SortItem("sheep",     "Sheep",     "🐑", "land"),
            SortItem("squirrel",  "Squirrel",  "🐿️", "land"),
            SortItem("pigeon",    "Pigeon",    "🕊️", "land"),
            SortItem("dolphin",   "Dolphin",   "🐬", "water"),
            SortItem("whale",     "Whale",     "🐋", "water"),
            SortItem("fish",      "Fish",      "🐟", "water"),
            SortItem("frog",      "Frog",      "🐸", "both"),
            SortItem("crocodile", "Crocodile", "🐊", "both"),
            SortItem("tortoise",  "Tortoise",  "🐢", "both"),
        ),
        controls = controls,
    )
}
