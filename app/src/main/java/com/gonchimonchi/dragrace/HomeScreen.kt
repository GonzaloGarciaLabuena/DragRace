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
import com.gonchimonchi.dragrace.activity.AddReinaActivity
import com.gonchimonchi.dragrace.activity.DeleteReinaActivity
import com.gonchimonchi.dragrace.activity.GestionarTemporadaActivity

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Log.i("DRAGRACE", "HomeScreen: vista")

        val context = LocalContext.current
        //Ver rankings
        Button(onClick = {
            context.startActivity(Intent(context, TablaRankingActivity::class.java))
        }) {
            Text("Ver ranking")
        }
        //Añadir reina a temporada
        Button(onClick = {
            context.startActivity(Intent(context, AddReinaActivity::class.java))
        }) {
            Text("Añadir reina")
        }

        //Eliminar reina
        Button(onClick = {
            context.startActivity(Intent(context, DeleteReinaActivity::class.java))
        }) {
            Text("Eliminar reinas")
        }

        //Gestionar temporadas
        Button(onClick = {
            context.startActivity(Intent(context, GestionarTemporadaActivity::class.java))
        }) {
            Text("Gestionar temporadas")
        }
    }
}
