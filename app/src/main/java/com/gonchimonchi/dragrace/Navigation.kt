package com.gonchimonchi.dragrace

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gonchimonchi.dragrace.viewmodel.DropboxViewModel
import com.gonchimonchi.dragrace.viewmodel.UploadScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        Log.i("DRAGRACE", "Navigation: vista")
        // Pantalla principal con 2 botones
        composable("home") {
            HomeScreen(
                modifier = modifier,
                onGoToRankings = { navController.navigate("rankings") },
                onGoToUpload = { navController.navigate("upload") }
            )
        }

        // Pantalla de subir imagen
        composable("upload") {
            val dropboxViewModel: DropboxViewModel = viewModel()
            UploadScreen(
                viewModel = dropboxViewModel,
                modifier = modifier,
                onUploadDone = { navController.popBackStack() }
            )
        }
    }
}

