package com.example.juegotriviav1

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Actividad para la segunda pregunta de la trivia.
 */
class Pregunta2Activity : AppCompatActivity() {

    private lateinit var buttons: List<Button>
    private var puntaje: Int = 0
    private var respondio: Boolean = false
    private var respondioCorrectamente: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 20000L
    private lateinit var tvTimer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pregunta_2)

        // Obtener el puntaje acumulado hasta ahora
        puntaje = intent.getIntExtra("PUNTAJE", 0)

        buttons = listOf(
            findViewById(R.id.bAfirmacion2_1),
            findViewById(R.id.bAfirmacion2_2),
            findViewById(R.id.bAfirmacion2_3),
            findViewById(R.id.bAfirmacion2_4)
        )

        // Configuración de colores visuales (Feedback) para respuestas correctas e incorrectas
        buttons[0].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)
        buttons[1].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)
        buttons[2].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_verde)// Respuesta Correcta
        buttons[3].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)

        val cardResultado = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardResultado)
        val textViewResultado = findViewById<TextView>(R.id.textViewResultado)
        tvTimer = findViewById(R.id.tvTimer)

        startTimer(cardResultado, textViewResultado)

        buttons.forEach { button ->
            button.setOnClickListener {
                selectButton(it as Button, cardResultado, textViewResultado)
            }
        }

        // Lógica para evaluar la respuesta y navegar a la siguiente pregunta
        findViewById<Button>(R.id.btnSiguiente).setOnClickListener {
            if (!respondio) {
                cardResultado.visibility = View.VISIBLE
                cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                textViewResultado.text = "Haga clic en alguna respuesta"
                return@setOnClickListener
            }

            if (respondioCorrectamente) {
                puntaje++
            }
            // Recuperar el historial anterior, añadir el resultado actual y enviarlo
            val respuestasAnteriores = intent.getBooleanArrayExtra("RESPUESTAS") ?: booleanArrayOf()
            val respuestas = respuestasAnteriores + respondioCorrectamente

            val intent = Intent(this, Pregunta3Activity::class.java)
            intent.putExtra("PUNTAJE", puntaje)
            intent.putExtra("RESPUESTAS", respuestas)
            startActivity(intent)
        }
    }

    /**
     * Maneja el estado de selección de los botones.
     */
    private fun selectButton(selectedButton: Button, cardResultado: com.google.android.material.card.MaterialCardView, textViewResultado: TextView) {
        if (respondio) return
        countDownTimer?.cancel()
        respondio = true
        buttons.forEach { it.isSelected = false }
        selectedButton.isSelected = true

        // REVELACIÓN: Siempre seleccionar también la respuesta correcta para mostrar su color (Verde)
        val correctButton = findViewById<Button>(R.id.bAfirmacion2_3)
        correctButton.isSelected = true

        // Guardar si la elección original fue la correcta para el puntaje
        respondioCorrectamente = (selectedButton.id == R.id.bAfirmacion2_3)

        // Deshabilitar todos los botones de respuesta para evitar cambios
        buttons.forEach { it.isEnabled = false }

        cardResultado.visibility = View.VISIBLE
        if (respondioCorrectamente) {

            textViewResultado.text = "✅ Correcto \n La exposición a pantallas de noche altera la melatonina y afecta el descanso, especialmente en adolescentes."
        } else {

            textViewResultado.text = "❌ Incorrecto \n La exposición a pantallas de noche altera la melatonina y afecta el descanso, especialmente en adolescentes."
        }

    }

    private fun startTimer(cardResultado: com.google.android.material.card.MaterialCardView, textViewResultado: TextView) {
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                tvTimer.text = "${seconds}s"
            }

            override fun onFinish() {
                tvTimer.text = "0s"
                if (!respondio) {
                    respondio = true
                    respondioCorrectamente = false
                    buttons.forEach { it.isEnabled = false }

                    val correctButton = findViewById<Button>(R.id.bAfirmacion2_3)
                    correctButton.isSelected = true

                    cardResultado.visibility = View.VISIBLE
                    cardResultado.setCardBackgroundColor(ContextCompat.getColor(this@Pregunta2Activity, android.R.color.holo_red_light))
                    textViewResultado.text = "⏰ ¡Tiempo agotado!\nLa exposición a pantallas de noche altera la melatonina y afecta el descanso, especialmente en adolescentes."
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}