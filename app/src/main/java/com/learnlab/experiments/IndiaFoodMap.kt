package com.learnlab.experiments

import androidx.compose.runtime.Composable
import com.learnlab.engines.SortBucket
import com.learnlab.engines.SortBuckets
import com.learnlab.engines.SortItem
import com.learnlab.store.ExperimentControls

@Composable
fun IndiaFoodMap(controls: ExperimentControls) {
    SortBuckets(
        prompt = "Traditional cuisines follow the crops grown locally. Match each dish to the Indian state " +
            "it traditionally comes from — and notice how the staples (rice, wheat, ragi, bamboo, etc.) shape the food.",
        bucketsTitle = "From",
        buckets = listOf(
            SortBucket("punjab",     "Punjab",     "Wheat, maize, chickpea country."),
            SortBucket("karnataka",  "Karnataka",  "Rice, ragi, urad, coconut."),
            SortBucket("manipur",    "Manipur",    "Rice, bamboo, soya bean."),
            SortBucket("kerala",     "Kerala",     "Coconut, rice, fish, spices."),
        ),
        items = listOf(
            SortItem("makki",   "Makki di roti",   "🌽", "punjab"),
            SortItem("sarson",  "Sarson da saag",  "🥬", "punjab"),
            SortItem("chhole",  "Chhole bhature",  "🫓", "punjab"),
            SortItem("lassi",   "Lassi",           "🥛", "punjab"),
            SortItem("idli",    "Idli",            "🍥", "karnataka"),
            SortItem("dosa",    "Dosa",            "🥞", "karnataka"),
            SortItem("ragi",    "Ragi mudde",      "🟫", "karnataka"),
            SortItem("eromba",  "Eromba",          "🥘", "manipur"),
            SortItem("utti",    "Utti",            "🫛", "manipur"),
            SortItem("appam",   "Appam",           "🍘", "kerala"),
            SortItem("meen",    "Meen curry",      "🐟", "kerala"),
            SortItem("payasam", "Payasam",         "🍮", "kerala"),
        ),
        controls = controls,
    )
}
