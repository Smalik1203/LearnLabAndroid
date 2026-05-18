package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.SortBucket
import com.learnlab.engines.SortBuckets
import com.learnlab.engines.SortItem
import com.learnlab.store.ExperimentControls

@Composable
fun PlantSorter(controls: ExperimentControls) {
    SortBuckets(
        prompt = "Group these plants the way the textbook does: by height and the type of stem. " +
            "Soft green stem → herb. Hard woody stem branching near the ground → shrub. " +
            "Hard thick stem branching higher up → tree.",
        bucketsTitle = "Plant group",
        buckets = listOf(
            SortBucket("herb",  "Herbs",  "Short. Soft, green, tender stems."),
            SortBucket("shrub", "Shrubs", "Medium. Hard stem, branches near the ground."),
            SortBucket("tree",  "Trees",  "Tall. Hard, thick, woody stem. Branches start higher up."),
        ),
        items = listOf(
            SortItem("mango",    "Mango",    "🥭", "tree"),
            SortItem("banyan",   "Banyan",   "🌳", "tree"),
            SortItem("neem",     "Neem",     "🌴", "tree"),
            SortItem("rose",     "Rose",     "🌹", "shrub"),
            SortItem("hibiscus", "Hibiscus", "🌺", "shrub"),
            SortItem("tulsi",    "Tulsi",    "🪴", "shrub"),
            SortItem("tomato",   "Tomato",   "🍅", "herb"),
            SortItem("mint",     "Mint",     "🌿", "herb"),
            SortItem("grass",    "Grass",    "🌾", "herb"),
        ),
        controls = controls,
    )
}
