package com.gonchimonchi.dragrace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.util.Log
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.gonchimonchi.dragrace.activity.TablaRankingActivity

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onGoToRankings: () -> Unit,
    onGoToUpload: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Log.i("DRAGRACE", "HomeScreen: vista")

        val context = LocalContext.current
        Button(onClick = {
            context.startActivity(Intent(context, TablaRankingActivity::class.java))
        }) {
            Text("Ver ranking")
        }
        Button(onClick = onGoToUpload) {
            Log.i("DRAGRACE", "HomeScreen: Upload boton presionado")
            Text("Subir imagen")
        }
    }
}
