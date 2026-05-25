package com.learnlab.lessons.figures.composeDraw

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Registry of Compose-drawn interactive figures. JSON refers to one of these
 * via `"assetType": "composeDraw", "asset": "<id>"`.
 *
 * Adding a new diagram:
 *  1. Build the Composable in a new file under this package.
 *  2. Register it here with a stable id.
 *  3. Reference it from chapter JSON.
 */
object ComposeFigureRegistry {

    /** Map of asset id → Composable that renders it full-area. */
    private val registry: Map<String, @Composable (Modifier) -> Unit> = mapOf(
        "LeafVenationComparator"   to { m -> LeafVenationComparator(m) },
        "PlantHeightsAtScale"      to { m -> PlantHeightsAtScale(m) },
        "RootSystemsExplorer"      to { m -> RootSystemsExplorer(m) },
        "CotyledonDissection"      to { m -> CotyledonDissection(m) },
        "PlantGroupingConceptMap"  to { m -> PlantGroupingConceptMap(m) },
    )

    fun has(id: String): Boolean = id in registry

    @Composable
    fun render(id: String, modifier: Modifier = Modifier) {
        val fn = registry[id] ?: return
        fn(modifier)
    }
}
