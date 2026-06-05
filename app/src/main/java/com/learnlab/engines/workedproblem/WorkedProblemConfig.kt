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
    /**
     * Common student follow-up questions for this step ("but why?").
     * Surfaced via a small `?` chip in the step strip; tapping opens
     * a modal that lets the teacher expand each one.
     *
     * Empty = no chip shown. Backward-compatible: configs that don't
     * include this field render exactly as before.
     */
    val whyAnswers: List<WhyAnswer> = emptyList(),
    /**
     * Optional cue for the teacher running the lesson — a suggested
     * question to ask the class before revealing the step, and/or a
     * common mistake to call out. Renders as a muted strip below the
     * prompt, visually distinct from student-facing text.
     *
     * Null = no teacher strip shown.
     */
    val teacherNote: TeacherNote? = null,
)

/**
 * Cues for the teacher, not the student. Both fields optional.
 *
 * `ask`     — a question to throw at the class before revealing the step
 *             ("What is the vertical velocity at the top?"). Helps shift
 *             the lesson from passive-watch to predict-then-reveal.
 * `mistake` — a common student error to address ("Many students use
 *             20 m/s as the upward speed by mistake"). Inoculates the
 *             class against the bug before it happens.
 */
@Serializable
data class TeacherNote(
    val ask: String? = null,
    val mistake: String? = null,
)

/**
 * One "but why?" question and its answer for a step.
 *
 * Keep questions short (student-voice, conversational) and answers
 * concise enough to read aloud from a tablet to a class. Maths
 * notation: ASCII / Unicode, same convention as `reveal`.
 */
@Serializable
data class WhyAnswer(
    val question: String,
    val answer: String,
)
