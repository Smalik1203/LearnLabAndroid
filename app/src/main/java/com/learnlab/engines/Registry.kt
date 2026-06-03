package com.learnlab.engines

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.learnlab.content.Experiment
import com.learnlab.experiments.AnimalMovement
import com.learnlab.experiments.HabitatSorter
import com.learnlab.experiments.IndiaFoodMap
import com.learnlab.experiments.PlantSorter
import com.learnlab.experiments.ComingSoonExperiment
import com.learnlab.store.ExperimentControls

typealias ExperimentComposable = @Composable (Experiment, ExperimentControls) -> Unit

/**
 * Mirrors src/runtime/ExperimentRegistry.ts. Every experiment id maps to a
 * native Composable. Where the React app has a custom bespoke screen we either
 * (a) port it natively or (b) render the ComingSoon placeholder for now —
 * same pattern the React app uses for unfinished experiments.
 */
val experimentRegistry: Map<String, ExperimentComposable> = mapOf(
    // Chapter 2 — drag/sort based, all reuse SortBuckets
    "plant-sorter"     to { _, c -> PlantSorter(c) },
    "animal-movement"  to { _, c -> AnimalMovement(c) },
    "habitat-sorter"   to { _, c -> HabitatSorter(c) },
    "india-food-map"   to { _, c -> IndiaFoodMap(c) },

    // Chapter 3 — reagent tests, all reuse LabReagentScreen
    "iodine-starch-test"  to { _, c -> com.learnlab.experiments.IodineStarchTest(c) },
    "fat-paper-test"      to { _, c -> com.learnlab.experiments.FatPaperTest(c) },
    "protein-violet-test" to { _, c -> com.learnlab.experiments.ProteinVioletTest(c) },

    // Chapter 1 + bespoke Chapter 2
    "scientific-method-detective" to { _, c -> com.learnlab.experiments.ScientificMethodDetective(c) },
    "plant-pattern"               to { _, c -> com.learnlab.experiments.PlantPattern(c) },
    "adaptation-lab"              to { _, c -> com.learnlab.experiments.AdaptationLab(c) },
    "deficiency-matchup"          to { _, c -> com.learnlab.experiments.DeficiencyMatchup(c) },

    // Chapter 4 — Physics Lab (all 4 fully ported)
    "projectile-motion" to { _, c -> com.learnlab.experiments.ProjectileMotion(c) },
    "simple-pendulum"   to { _, c -> com.learnlab.experiments.SimplePendulum(c) },
    "convex-lens"       to { _, c -> com.learnlab.experiments.ConvexLens(c) },
    "em-induction"      to { _, c -> com.learnlab.experiments.EMInduction(c) },
    "oersted-experiment" to { _, c -> com.learnlab.experiments.OerstedExperiment(c) },
    "electromagnet"     to { _, c -> com.learnlab.experiments.Electromagnet(c) },

    // Chapter 5 — Grade 8 magnetism + motor
    "magnetic-motor"    to { _, c -> com.learnlab.experiments.MotorFunctioning(c) },

    // Chapter 6 — Cell biology / the invisible living world
    "cell-explorer"     to { _, c -> com.learnlab.experiments.CellExplorer(c) },
    "soil-suspension"   to { _, c -> com.learnlab.experiments.SoilSuspension(c) },
    "microbe-guide"     to { _, c -> com.learnlab.experiments.MicrobeGuide(c) },
    "curd-formation"    to { _, c -> com.learnlab.experiments.CurdFormation(c) },

    // Chapter 7 — Grade 8 Health: Ultimate Treasure
    "health-habits"            to { _, c -> com.learnlab.experiments.HealthHabits(c) },
    "diseases-classify-spread" to { _, c -> com.learnlab.experiments.DiseaseClassifier(c) },

    // Still placeholders (later turns)
    "leaf-venation"   to { e, _ -> ComingSoonExperiment(e) },
    "root-system"     to { e, _ -> ComingSoonExperiment(e) },
    "seed-dissection" to { e, _ -> ComingSoonExperiment(e) },
    "balanced-thali"  to { e, _ -> ComingSoonExperiment(e) },
    "junk-vs-nutri"   to { e, _ -> ComingSoonExperiment(e) },
    "food-miles"      to { e, _ -> ComingSoonExperiment(e) },
)

// unused guard kept so this file imports a Color and matches the React style sheet
@Suppress("unused")
private val _kept = Color.Unspecified
