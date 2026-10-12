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
 * Actividad para la primera pregunta de la trivia.
 * Maneja la lógica de selección de respuesta, validación de puntaje y navegación.
 */
class Pregunta1Activity : AppCompatActivity() {

    private lateinit var buttons: List<Button>
    private var puntaje: Int = 0
    private var respondio: Boolean = false
    private var respondioCorrectamente: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 20000L
    private lateinit var tvTimer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pregunta_1)

        // Recuperar el puntaje actual enviado desde la actividad anterior
        puntaje = intent.getIntExtra("PUNTAJE", 0)

        // Lista de botones de opciones para facilitar su manejo conjunto
        buttons = listOf(
            findViewById(R.id.bAfirmacion1_1),
            findViewById(R.id.bAfirmacion1_2),
            findViewById(R.id.bAfirmacion1_3),
            findViewById(R.id.bAfirmacion1_4)
        )

        // Asignación de colores visuales (Feedback): Verde para la correcta, Rojo para las incorrectas.
        // Se utiliza backgroundTintList con selectores definidos en recursos XML.
        buttons[0].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)
        buttons[1].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_verde) // Respuesta Correcta
        buttons[2].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)
        buttons[3].backgroundTintList = ContextCompat.getColorStateList(this, R.color.selector_rojo)

        val cardResultado = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardResultado)
        val textViewResultado = findViewById<TextView>(R.id.textViewResultado)
        tvTimer = findViewById(R.id.tvTimer)

        // Iniciar temporizador de 20 segundos
        startTimer(cardResultado, textViewResultado)

        // Configurar el evento click para cada botón de opción
        buttons.forEach { button ->
            button.setOnClickListener {
                selectButton(it as Button, cardResultado, textViewResultado)
            }
        }

        // Lógica para avanzar a la siguiente pantalla
        findViewById<Button>(R.id.btnSiguiente).setOnClickListener {
            if (!respondio) {
                cardResultado.visibility = View.VISIBLE
                cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                textViewResultado.text = "Haga clic en alguna respuesta"
                return@setOnClickListener
            }

            // Verificar si el usuario respondió correctamente antes de avanzar
            if (respondioCorrectamente) {
                puntaje++
            }
            // Navegación hacia la segunda pregunta, pasando el puntaje actualizado y las respuestas
            val respuestas = booleanArrayOf(respondioCorrectamente)
            val intent = Intent(this, Pregunta2Activity::class.java)
            intent.putExtra("PUNTAJE", puntaje)
            intent.putExtra("RESPUESTAS", respuestas)
            startActivity(intent)
        }
    }

    /**
     * Gestiona el estado visual de selección: desmarca todos y marca el actual.
     */
    private fun selectButton(selectedButton: Button, cardResultado: com.google.android.material.card.MaterialCardView, textViewResultado: TextView) {
        if (respondio) return
        countDownTimer?.cancel()
        respondio = true
        // Deseleccionar todos los botones para asegurar que solo uno esté marcado
        buttons.forEach { it.isSelected = false }
        
        // Seleccionar el botón clickeado
        selectedButton.isSelected = true

        // REVELACIÓN: Siempre seleccionar también la respuesta correcta para mostrar su color (Verde)
        val correctButton = findViewById<Button>(R.id.bAfirmacion1_2)
        correctButton.isSelected = true

        // Guardar si la elección original fue la correcta para el puntaje
        respondioCorrectamente = (selectedButton.id == R.id.bAfirmacion1_2)

        // Deshabilitar todos los botones de respuesta para evitar cambios
        buttons.forEach { it.isEnabled = false }

        cardResultado.visibility = View.VISIBLE
        if (respondioCorrectamente) {

            textViewResultado.text = "✅ Correcto \n Reemplazar vínculos humanos por chatbots es una señal de dependencia emocional hacia la IA."
        } else {

            textViewResultado.text = "❌ Incorrecto \n Reemplazar vínculos humanos por chatbots es una señal de dependencia emocional hacia la IA."
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

                    val correctButton = findViewById<Button>(R.id.bAfirmacion1_2)
                    correctButton.isSelected = true

                    cardResultado.visibility = View.VISIBLE
                    cardResultado.setCardBackgroundColor(ContextCompat.getColor(this@Pregunta1Activity, android.R.color.holo_red_light))
                    textViewResultado.text = "⏰ ¡Tiempo agotado!\nReemplazar vínculos humanos por chatbots es una señal de dependencia emocional hacia la IA."
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}