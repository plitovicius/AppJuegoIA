package com.example.juegotriviav1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.juegotriviav1.data.ScoreManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Actividad final que muestra los resultados obtenidos por el jugador.
 * Permite ver el puntaje total, guarda la partida en el ranking del Top 3
 * y ofrece la opción de reiniciar el juego.
 */
class FinalJuegoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_juego)

        // Se recibe el puntaje final acumulado y el tiempo total empleado
        val puntaje = intent.getIntExtra("PUNTAJE", 0)
        val tiempoTotal = intent.getIntExtra("TOTAL_TIME", 0)
        // Se recibe el array con los aciertos (true) o errores (false)
        val respuestas = intent.getBooleanArrayExtra("RESPUESTAS") ?: booleanArrayOf()

        // Guardar la partida en el Top 3 usando ScoreManager con fecha, hora, aciertos y tiempo
        val dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ScoreManager(this).saveScore(puntaje, tiempoTotal, dateTimeStr)

        // Mostrar el puntaje en el formato "acertadas/total" dentro del círculo
        val textViewPuntajeCirculo = findViewById<TextView>(R.id.textViewPuntajeCirculo)
        textViewPuntajeCirculo.text = "$puntaje de 5"

        // Referencias a los emojis de estado en el ScrollView
        val estados = listOf(
            findViewById<TextView>(R.id.tvEstado1),
            findViewById<TextView>(R.id.tvEstado2),
            findViewById<TextView>(R.id.tvEstado3),
            findViewById<TextView>(R.id.tvEstado4),
            findViewById<TextView>(R.id.tvEstado5)
        )

        // Actualizar cada emoji según el resultado guardado
        respuestas.forEachIndexed { index, correcto ->
            if (index < estados.size) {
                estados[index].text = if (correcto) "✅" else "❌"
            }
        }

        // Botón para volver al inicio del juego, limpiando el historial de actividades
        findViewById<Button>(R.id.btnReiniciar).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // FLAG_ACTIVITY_CLEAR_TOP asegura que no se acumulen pantallas viejas en la pila
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
