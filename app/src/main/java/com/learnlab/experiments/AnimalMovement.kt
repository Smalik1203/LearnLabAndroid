package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.SortBucket
import com.learnlab.engines.SortBuckets
import com.learnlab.engines.SortItem
import com.learnlab.store.ExperimentControls

@Composable
fun AnimalMovement(controls: ExperimentControls) {
    SortBuckets(
        prompt = "Which body part does each animal mainly use to move? Match the animal to its primary locomotion category.",
        bucketsTitle = "Moves using",
        buckets = listOf(
            SortBucket("legs",  "Legs",  "Walks, runs, hops, or jumps."),
            SortBucket("wings", "Wings", "Flies — wings do most of the work."),
            SortBucket("fins",  "Fins",  "Swims with streamlined body and fins."),
            SortBucket("body",  "Whole body", "Crawls, slithers, no limbs needed."),
        ),
        items = listOf(
            SortItem("goat",      "Goat",      "🐐", "legs"),
            SortItem("ant",       "Ant",       "🐜", "legs"),
            SortItem("kangaroo",  "Kangaroo",  "🦘", "legs"),
            SortItem("eagle",     "Eagle",     "🦅", "wings"),
            SortItem("housefly",  "Housefly",  "🪰", "wings"),
            SortItem("butterfly", "Butterfly", "🦋", "wings"),
            SortItem("fish",      "Fish",      "🐟", "fins"),
            SortItem("shark",     "Shark",     "🦈", "fins"),
            SortItem("snake",     "Snake",     "🐍", "body"),
            SortItem("earthworm", "Earthworm", "🪱", "body"),
        ),
        controls = controls,
    )
}
