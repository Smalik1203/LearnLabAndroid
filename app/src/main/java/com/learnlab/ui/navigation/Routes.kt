package com.learnlab.ui.navigation

object Routes {
    const val HOME         = "home"
    const val GRADE_SELECT = "grade-select/{subject}"
    const val CURRICULUM   = "curriculum/{grade}"
    const val CHAPTER      = "chapter/{chapterId}"
    const val LESSON       = "lesson/{experimentId}"

    fun gradeSelect(subject: String) = "grade-select/$subject"
    fun curriculum(grade: Int) = "curriculum/$grade"
    fun chapter(chapterId: String) = "chapter/$chapterId"
    fun lesson(experimentId: String) = "lesson/$experimentId"
}
