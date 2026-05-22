package com.learnlab.ui.navigation

object Routes {
    const val HOME       = "home"
    const val CURRICULUM = "curriculum/{grade}"
    const val LESSON     = "lesson/{experimentId}"
    const val SETTINGS   = "settings"

    fun curriculum(grade: Int) = "curriculum/$grade"
    fun lesson(experimentId: String) = "lesson/$experimentId"
}
