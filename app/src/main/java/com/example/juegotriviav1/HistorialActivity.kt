package com.example.juegotriviav1

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.juegotriviav1.data.ScoreManager

/**
 * Actividad que muestra el historial completo de todas las partidas jugadas
 * utilizando un RecyclerView para una lista fluida y eficiente.
 */
class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar todas las partidas desde el ScoreManager
        val historyList = ScoreManager(this).getAllScores()
        recyclerView.adapter = HistorialAdapter(historyList)

        // Botón para volver
        findViewById<Button>(R.id.btnVolverHistorial).setOnClickListener {
            finish()
        }
    }
}
