package com.learnlab.ui.navigation

object Routes {
    const val HOME       = "home"
    const val CURRICULUM = "curriculum/{grade}"
    const val CHAPTER    = "chapter/{chapterId}"
    const val LESSON     = "lesson/{experimentId}"

    fun curriculum(grade: Int) = "curriculum/$grade"
    fun chapter(chapterId: String) = "chapter/$chapterId"
    fun lesson(experimentId: String) = "lesson/$experimentId"
}
