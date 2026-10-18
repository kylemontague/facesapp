package watch.facesapp.mobile.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import watch.facesapp.mobile.ui.camera.CameraScreen
import watch.facesapp.mobile.ui.customise.CustomiseScreen
import watch.facesapp.mobile.ui.customise.CustomiseViewModel
import watch.facesapp.mobile.ui.home.HomeScreen
import watch.facesapp.mobile.ui.home.HomeViewModel
import watch.facesapp.mobile.ui.splash.SplashScreen

@Composable
fun FacesApp(
    deepLinkUri: android.net.Uri? = null,
    onClearDeepLink: () -> Unit = {}
) {
    val navController = rememberNavController()
    val customiseViewModel: CustomiseViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (deepLinkUri != null) "home" else "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCamera = { templateStyleId ->
                    customiseViewModel.setFixedTemplate(templateStyleId)
                    navController.navigate("camera")
                },
                onNavigateToCustomise = { templateStyleId ->
                    customiseViewModel.setFixedTemplate(templateStyleId)
                    navController.navigate("customise")
                },
                deepLinkUri = deepLinkUri,
                onClearDeepLink = onClearDeepLink
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
