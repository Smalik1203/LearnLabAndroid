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
import com.learnlab.content.AllExperiments
import com.learnlab.store.AppState
import com.learnlab.ui.chapter.ChapterScreen
import com.learnlab.ui.curriculum.CurriculumScreen
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
                onScienceClick = { navController.navigate(Routes.curriculum(6)) },
            )
        }

        composable(
            route = Routes.CURRICULUM,
            arguments = listOf(navArgument("grade") { type = NavType.IntType }),
        ) {
            CurriculumScreen(
                state = state,
                onExperimentSelected = { id -> navController.navigate(Routes.lesson(id)) },
                onChapterSelected = { chapterId -> navController.navigate(Routes.chapter(chapterId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CHAPTER,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType }),
        ) { backStack ->
            val chapterId = backStack.arguments?.getString("chapterId") ?: return@composable
            ChapterScreen(
                state = state,
                chapterId = chapterId,
                onBack = { navController.popBackStack() },
                onOpenActivity = { id -> navController.navigate(Routes.lesson(id)) },
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("experimentId") { type = NavType.StringType }),
        ) { backStack ->
            val experimentId = backStack.arguments?.getString("experimentId") ?: return@composable
            val ids = AllExperiments.map { it.id }
            val idx = ids.indexOf(experimentId)

            LessonScreen(
                state        = state,
                experimentId = experimentId,
                onBack       = { navController.popBackStack() },
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
