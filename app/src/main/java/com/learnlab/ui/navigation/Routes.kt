package com.learnlab.ui.navigation

object Routes {
    const val LANDING      = "landing"
    const val HOME         = "home"
    const val HISTORY      = "history"
    const val GRADE_SELECT = "grade-select/{subject}"
    const val CURRICULUM   = "curriculum/{grade}"
    const val LESSON       = "lesson/{experimentId}"
    const val CHAPTER_READER = "chapter-reader/{chapterId}"

    fun gradeSelect(subject: String) = "grade-select/$subject"
    fun curriculum(grade: Int) = "curriculum/$grade"
    fun lesson(experimentId: String) = "lesson/$experimentId"
    fun chapterReader(chapterId: String) = "chapter-reader/$chapterId"
}
