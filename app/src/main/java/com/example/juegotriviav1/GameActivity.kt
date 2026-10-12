package com.example.juegotriviav1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.juegotriviav1.viewmodel.GameViewModel
import com.google.android.material.card.MaterialCardView

/**
 * Actividad principal del juego que actúa como la Vista (View) dentro de la arquitectura MVVM.
 * Observa los estados emitidos por el [GameViewModel] y actualiza dinámicamente la interfaz gráfica
 * (preguntas, opciones, colores temáticos, temporizador y resultados).
 */
class GameActivity : AppCompatActivity() {

     // Instancia del ViewModel asociada al ciclo de vida de la actividad
    private val viewModel: GameViewModel by viewModels()

    // Tiempo de inicio de la partida para calcular la duración total
    private val startTime = System.currentTimeMillis()

    // Referencias a los componentes de la interfaz de usuario (UI)
    private lateinit var buttons: List<Button>
    private var tvTimer: TextView? = null
    private lateinit var progressBarTimer: android.widget.ProgressBar
    private lateinit var tvProgreso: TextView
    private lateinit var textViewPregunta: TextView
    private lateinit var cardResultado: MaterialCardView
    private lateinit var textViewResultado: TextView
    private lateinit var btnSiguiente: Button
    private lateinit var viewHeader: View
    private lateinit var rootLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        observeViewModel()
    }

    /** Inicializa las vistas de la interfaz y configura los eventos de clic. */
    private fun initViews() {
        // Lista de botones de opciones de respuesta
        buttons = listOf(
            findViewById(R.id.bAfirmacion1),
            findViewById(R.id.bAfirmacion2),
            findViewById(R.id.bAfirmacion3),
            findViewById(R.id.bAfirmacion4)
        )
        tvTimer = findViewById(R.id.tvTimer)
        progressBarTimer = findViewById(R.id.progressBarTimer)
        tvProgreso = findViewById(R.id.tvProgreso)
        textViewPregunta = findViewById(R.id.textViewPregunta)
        cardResultado = findViewById(R.id.cardResultado)
        textViewResultado = findViewById(R.id.textViewResultado)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        viewHeader = findViewById(R.id.viewHeader)
        rootLayout = findViewById(R.id.gameRootLayout)

        // Asignar el listener de clic a cada botón de respuesta
        buttons.forEachIndexed { index, button ->
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.boton_background_selector)
            button.setOnClickListener {
                viewModel.submitAnswer(index)
            }
        }

        // Configurar el botón "Siguiente" para avanzar o terminar la partida
        btnSiguiente.setOnClickListener {
            val isAnswered = viewModel.isAnswered.value == true
            if (!isAnswered) {
                cardResultado.visibility = View.VISIBLE
                cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                textViewResultado.text = "Haga clic en alguna respuesta"
                return@setOnClickListener
            }

            val hasNext = viewModel.nextQuestion()
            if (!hasNext) {
                navigateToFinalScreen()
            }
        }
    }

    /** Observa los LiveData del ViewModel para reaccionar ante cambios de estado. */
    private fun observeViewModel() {
        // Observa el tiempo restante del temporizador proveniente del ViewModel
        viewModel.timeLeft.observe(this) { seconds ->
            tvTimer?.text = "${seconds}s"
            progressBarTimer.progress = seconds.toInt()

            // Cambio dinámico de color según el tiempo restante
            val colorHex = when {
                seconds > 10 -> "#4CAF50"  // Verde (> 10 segundos)
                seconds in 6..10 -> "#FACC15" // Amarillo (de 6 a 10 segundos)
                else -> "#EF4444"          // Rojo (5 segundos o menos)
            }
            progressBarTimer.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(colorHex))
        }

        // Observa cambios en la pregunta actual
        viewModel.currentQuestion.observe(this) { question ->
            if (question == null) return@observe

            val index = viewModel.currentIndex.value ?: 0
            val total = viewModel.getTotalQuestions()
            tvProgreso.text = "${index + 1}/$total"
            textViewPregunta.text = question.question

            // Actualizar textos y estados de habilitación de los botones de opciones
            question.options.forEachIndexed { idx, optionText ->
                if (idx < buttons.size) {
                    buttons[idx].text = optionText
                    buttons[idx].isEnabled = true
                    buttons[idx].isSelected = false
                }
            }

            // Aplicar colores temáticos personalizados definidos en el JSON
            try {
                viewHeader.setBackgroundColor(Color.parseColor(question.headerColor))
                rootLayout.setBackgroundColor(Color.parseColor(question.backgroundColor))
                btnSiguiente.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
                btnSiguiente.setBackgroundColor(Color.parseColor(question.headerColor))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            cardResultado.visibility = View.GONE
        }

        // Observa cuando la pregunta es respondida (por el usuario o por tiempo agotado)
        viewModel.isAnswered.observe(this) { answered ->
            if (answered) {
                val question = viewModel.currentQuestion.value ?: return@observe
                val selectedIdx = viewModel.selectedOptionIndex.value ?: -1
                val isCorrect = viewModel.isCorrect.value == true

                // Si el tiempo se agotó (selectedIdx == -1), reproducir sonido de alerta/campana
                if (selectedIdx == -1) {
                    playBellSound()
                }

                // Deshabilitar botones y resaltar la respuesta correcta en verde
                buttons.forEachIndexed { idx, btn ->
                    btn.isEnabled = false
                    if (idx == question.correctIndex) {
                        btn.isSelected = true
                    }
                }

                // Desmarcar visualmente si el usuario eligió una opción incorrecta
                if (selectedIdx >= 0 && selectedIdx < buttons.size && selectedIdx != question.correctIndex) {
                    buttons[selectedIdx].isSelected = false
                }

                // Mostrar la tarjeta con el resultado y su respectiva explicación
                cardResultado.visibility = View.VISIBLE
                if (isCorrect) {
                    cardResultado.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                    textViewResultado.text = "✅ Correcto \n ${question.explanation}"
                } else {
                    cardResultado.setCardBackgroundColor(
                        if (selectedIdx == -1) ContextCompat.getColor(this, android.R.color.holo_red_light)
                        else ContextCompat.getColor(this, android.R.color.holo_red_light)
                    )
                    val prefix = if (selectedIdx == -1) "⏰ ¡Tiempo agotado!\n" else "❌ Incorrecto \n "
                    textViewResultado.text = prefix + question.explanation
                }
            }
        }
    }

    /** Reproduce un sonido de alerta o campana al finalizar el temporizador. */
    private fun playBellSound() {
        try {
            // Nota: Se utiliza el sonido de notificación por defecto del sistema.
            // Si deseas un archivo de audio personalizado, puedes crear la carpeta res/raw/ y usar:
            // val mediaPlayer = android.media.MediaPlayer.create(this, R.raw.campana)
            // mediaPlayer?.start()
            // mediaPlayer?.setOnCompletionListener { it.release() }

            val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val r = android.media.RingtoneManager.getRingtone(this, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Navega hacia la pantalla final pasando el puntaje obtenido y el array de resultados. */
    private fun navigateToFinalScreen() {
        val score = viewModel.getScore()
        val resultsArray = viewModel.results.toBooleanArray()
        val totalTimeSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()

        val intent = Intent(this, FinalJuegoActivity::class.java)
        intent.putExtra("PUNTAJE", score)
        intent.putExtra("RESPUESTAS", resultsArray)
        intent.putExtra("TOTAL_TIME", totalTimeSeconds)
        startActivity(intent)
        finish()
    }
}
