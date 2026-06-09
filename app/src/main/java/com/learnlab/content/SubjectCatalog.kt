package com.learnlab.content

import androidx.compose.ui.graphics.Color
import com.learnlab.android.R
import com.learnlab.design.SubjectBiology
import com.learnlab.design.SubjectChemistry
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics

/**
 * The web Learn hub presents four subjects (Physics, Chemistry, Mathematics,
 * Biology). The Android content is a single NCERT "Science" set; this catalog
 * maps each existing experiment onto a subject so the web's subject → grade →
 * topic flow works on top of the current data. Additive only — content/Experiments.kt
 * is untouched.
 */
enum class Subject(
    val displayName: String,
    val description: String,
    val color: Color,
    val iconRes: Int,
) {
    PHYSICS(
        "Physics",
        "Explore motion, forces, waves, and the fundamental laws that govern our universe.",
        SubjectPhysics,
        R.drawable.subject_physics,
    ),
    CHEMISTRY(
        "Chemistry",
        "Discover reactions, molecules, and the building blocks of matter through virtual labs.",
        SubjectChemistry,
        R.drawable.subject_chemistry,
    ),
    MATHEMATICS(
        "Mathematics",
        "Visualize patterns, geometry, and abstract concepts through interactive demonstrations.",
        SubjectMath,
        R.drawable.subject_mathematics,
    ),
    BIOLOGY(
        "Biology",
        "Experience life processes, ecosystems, and the wonders of living organisms.",
        SubjectBiology,
        R.drawable.subject_biology,
    ),
}

/** Which subject each experiment belongs to (by experiment id). */
private val experimentSubject: Map<String, Subject> = buildMap {
    // Physics — motion, magnetism, electricity, optics, scientific method
    listOf(
        "scientific-method-detective", "projectile-motion", "simple-pendulum",
        "magnetic-motor", "oersted-experiment", "electromagnet", "em-induction",
        "convex-lens",
    ).forEach { put(it, Subject.PHYSICS) }

    // Chemistry — food chemical tests / reactions
    listOf(
        "iodine-starch-test", "fat-paper-test", "protein-violet-test",
    ).forEach { put(it, Subject.CHEMISTRY) }

    // Biology — living world, nutrition, microbes, health
    listOf(
        "plant-sorter", "leaf-venation", "root-system", "seed-dissection",
        "plant-pattern", "animal-movement", "habitat-sorter", "adaptation-lab",
        "deficiency-matchup", "balanced-thali", "india-food-map", "junk-vs-nutri",
        "food-miles", "cell-explorer", "soil-suspension", "microbe-guide",
        "curd-formation", "health-habits", "diseases-classify-spread",
    ).forEach { put(it, Subject.BIOLOGY) }
}

private val experimentGrade: Map<String, Int> =
    Chapters.flatMap { ch -> ch.experiments.map { it.id to ch.grade } }.toMap()

fun subjectOf(experimentId: String): Subject? = experimentSubject[experimentId]

/** A subject has live content only if at least one of its experiments exists. */
fun Subject.hasContent(): Boolean = experimentSubject.containsValue(this)

/** Grades (sorted) that have at least one topic in this subject. */
fun gradesFor(subject: Subject): List<Int> =
    experimentSubject.entries
        .filter { it.value == subject }
        .mapNotNull { experimentGrade[it.key] }
        .distinct()
        .sorted()

/** Topics (experiments) for a subject within a grade. */
fun topicsFor(subject: Subject, grade: Int): List<Experiment> =
    AllExperiments.filter {
        experimentSubject[it.id] == subject && experimentGrade[it.id] == grade
    }
