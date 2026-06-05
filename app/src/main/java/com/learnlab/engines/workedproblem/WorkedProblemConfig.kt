package com.learnlab.engines.workedproblem

import kotlinx.serialization.Serializable

/**
 * JSON-driven config for a single worked-problem walkthrough.
 *
 * Pedagogy: teacher reads the problem, asks the class to attempt on paper,
 * then taps "Reveal step" on each step in order. The class checks their
 * working against the reveal. Final answer slides up after the last step.
 *
 * Keep math notation here ASCII / Unicode (e.g. "u_y", "θ", "·") — no LaTeX,
 * no MathJax. The classroom projector renders system fonts.
 */
@Serializable
data class WorkedProblemConfig(
    /** Full problem statement, shown in the problem card on the right. */
    val problem: String,
    /** Given quantities — rendered as a small table under "GIVEN". */
    val given: List<Quantity> = emptyList(),
    /** Quantities to find — rendered as a small table under "FIND". */
    val find: List<Quantity> = emptyList(),
    /**
     * Which diagram renderer to use in the left pane. Today only
     * "projectile-launch" is implemented; future engines may add others.
     */
    val diagram: String,
    /** The step-by-step solution. Revealed one step at a time. */
    val steps: List<Step>,
    /** Final consolidated answer, shown after the last step is revealed. */
    val finalAnswer: String,
    /**
     * Optional shortcut / formula that connects the steps to a JEE-style
     * one-liner. Shown behind a "Bonus formula" toggle.
     */
    val bonus: String? = null,
)

@Serializable
data class Quantity(
    /** Symbol, e.g. "u_y" or "θ". */
    val sym: String,
    /**
     * For "given": the value with units, e.g. "20 m/s".
     * For "find": leave null and use `label` instead.
     */
    val value: String? = null,
    /** Human label, e.g. "time of flight". Used in the "FIND" list. */
    val label: String? = null,
)

@Serializable
data class Step(
    /** Always visible. The prompt the teacher reads to the class. */
    val prompt: String,
    /**
     * Hidden until the teacher taps "Reveal step ▶". Each list entry
     * renders as its own line — useful for formula → substitution → result.
     */
    val reveal: List<String>,
    /**
     * Optional diagram annotation key. The diagram renderer toggles
     * elements on as steps reveal. Keys are engine/diagram-specific —
     * for "projectile-launch": "components", "trajectory", "apex",
     * "timeOfFlight", "maxHeight", "range".
     *
     * Null means "no diagram change for this step".
     */
    val annotation: String? = null,
)
