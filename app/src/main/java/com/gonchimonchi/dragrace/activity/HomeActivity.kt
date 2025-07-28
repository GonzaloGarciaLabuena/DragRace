package com.gonchimonchi.dragrace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.gonchimonchi.dragrace.activity.*
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val app = applicationContext as ViewModelStoreOwner
        val temporadaViewModel = ViewModelProvider(app)[TemporadaViewModel::class.java]

        val progressLayout = findViewById<View>(R.id.progressBarLayout)
        val contenidoLayout = findViewById<View>(R.id.contenidoLayout)

        lifecycleScope.launch {
            temporadaViewModel.cargarSeasonsConReinas()
            progressLayout.visibility = View.GONE
            contenidoLayout.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btnVerRanking).setOnClickListener {
            startActivity(Intent(this, TablaRankingActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddReina).setOnClickListener {
            startActivity(Intent(this, AddReinaActivity::class.java))
        }

        findViewById<Button>(R.id.btnEliminarReinas).setOnClickListener {
            startActivity(Intent(this, DeleteReinaActivity::class.java))
        }

        findViewById<Button>(R.id.btnGestionarTemporadas).setOnClickListener {
            startActivity(Intent(this, GestionarTemporadaActivity::class.java))
        }
    }
}
