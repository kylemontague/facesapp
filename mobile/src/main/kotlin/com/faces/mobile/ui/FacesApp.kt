package com.faces.mobile.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.faces.mobile.ui.camera.CameraScreen
import com.faces.mobile.ui.customise.CustomiseScreen
import com.faces.mobile.ui.customise.CustomiseViewModel
import com.faces.mobile.ui.home.HomeScreen
import com.faces.mobile.ui.home.HomeViewModel

@Composable
fun FacesApp() {
    val navController = rememberNavController()
    val customiseViewModel: CustomiseViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToCustomise = { faceId ->
                    customiseViewModel.loadExistingFace(faceId)
                    navController.navigate("customise")
                }
            )
        }

        composable("camera") {
            CameraScreen(
                onPhotoCaptured = { palette ->
                    customiseViewModel.initializeWithPalette(palette)
                    navController.navigate("customise") {
                        popUpTo("home")
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable("customise") {
            CustomiseScreen(
                viewModel = customiseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
