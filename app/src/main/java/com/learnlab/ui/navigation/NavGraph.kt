package com.learnlab.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.learnlab.content.Chapters
import com.learnlab.content.findExperiment
import com.learnlab.store.AppState
import com.learnlab.ui.curriculum.CurriculumScreen
import com.learnlab.ui.grade.GradeSelectScreen
import com.learnlab.ui.home.HomeScreen
import com.learnlab.ui.lesson.LessonScreen

@Composable
fun LearnLabNavGraph(navController: NavHostController, state: AppState) {
    NavHost(
        navController    = navController,
        startDestination = Routes.HOME,
        enterTransition  = { slideInHorizontally { it } + fadeIn() },
        exitTransition   = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition  = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition   = { slideOutHorizontally { it } + fadeOut() },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                state = state,
                onScienceClick = { navController.navigate(Routes.gradeSelect("science")) },
            )
        }

        composable(
            route = Routes.GRADE_SELECT,
            arguments = listOf(navArgument("subject") { type = NavType.StringType }),
        ) { backStack ->
            val subject = backStack.arguments?.getString("subject") ?: "science"
            GradeSelectScreen(
                state = state,
                subject = subject,
                onGradeSelected = { grade -> navController.navigate(Routes.curriculum(grade)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CURRICULUM,
            arguments = listOf(navArgument("grade") { type = NavType.IntType }),
        ) { backStack ->
            val grade = backStack.arguments?.getInt("grade") ?: 6
            CurriculumScreen(
                state = state,
                grade = grade,
                onExperimentSelected = { id -> navController.navigate(Routes.lesson(id)) },
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("experimentId") { type = NavType.StringType }),
        ) { backStack ->
            val experimentId = backStack.arguments?.getString("experimentId") ?: return@composable
            val chapter = findExperiment(experimentId)?.let { exp ->
                Chapters.firstOrNull { it.id == exp.chapterId }
            }
            val ids = chapter?.experiments?.map { it.id } ?: emptyList()
            val idx = ids.indexOf(experimentId)

            LessonScreen(
                state        = state,
                experimentId = experimentId,
                onBack       = { navController.popBackStack() },
                onHome       = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onPrev       = if (idx > 0) {
                    {
                        navController.navigate(Routes.lesson(ids[idx - 1])) {
                            popUpTo(Routes.lesson(experimentId)) { inclusive = true }
                        }
                    }
                } else null,
                onNext       = if (idx < ids.size - 1) {
                    {
                        navController.navigate(Routes.lesson(ids[idx + 1])) {
                            popUpTo(Routes.lesson(experimentId)) { inclusive = true }
                        }
                    }
                } else null,
            )
        }
    }
}
