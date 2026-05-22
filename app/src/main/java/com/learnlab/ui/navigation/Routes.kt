package com.learnlab.ui.navigation

object Routes {
    const val HOME     = "home"
    const val BROWSER  = "browser/{grade}/{subjectId}"
    const val LESSON   = "lesson/{experimentId}"
    const val SETTINGS = "settings"

    fun browser(grade: Int, subjectId: String) = "browser/$grade/$subjectId"
    fun lesson(experimentId: String) = "lesson/$experimentId"
}
