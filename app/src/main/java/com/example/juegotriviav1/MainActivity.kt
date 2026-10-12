package com.example.juegotriviav1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Pantalla principal del juego de trivia.
 * Muestra los textos arriba, reproduce un video ampliado con volumen máximo,
 * actualiza su barra de progreso, muestra el Top 3 de mejores puntajes del usuario (mediante fragmento),
 * permite ver el historial completo de partidas y gestiona el flujo del botón "Comenzar".
 */
class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var videoProgressBar: ProgressBar
    private lateinit var btnComenzar: Button
    private val handler = Handler(Looper.getMainLooper())
    
    // Inicialización directa del Runnable para evitar errores de inicialización tardía (lateinit)
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            if (::videoView.isInitialized && videoView.isPlaying) {
                val current = videoView.currentPosition
                val duration = videoView.duration
                if (duration > 0) {
                    val percent = (current * 100) / duration
                    videoProgressBar.progress = percent
                }
            }
            handler.postDelayed(this, 100) // Actualizar cada 100ms
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configurar fondo degradado
        val fondo = findViewById<ConstraintLayout>(R.id.miContenedorPrincipal)
        fondo.setBackgroundResource(R.drawable.fondo_degradado)

        // 2. Referencias a vistas
        videoView = findViewById(R.id.videoView)
        videoProgressBar = findViewById(R.id.videoProgressBar)
        btnComenzar = findViewById(R.id.button)

        // 3. Configurar el botón "Comenzar" para navegar al juego
        btnComenzar.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        // 4. Configurar el botón "Ver Historial" para abrir la lista completa de partidas
        findViewById<Button>(R.id.btnVerHistorial).setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // El botón "Comenzar" inicia oculto al volver a la pantalla principal
        btnComenzar.visibility = View.GONE
        videoProgressBar.progress = 0

        // Configurar y reiniciar la reproducción del video
        val uri = Uri.parse("android.resource://$packageName/${R.raw.intro}")
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(1.0f, 1.0f) // Volumen máximo del sistema
        }

        // Cuando el video termina, hacer visible el botón "Comenzar"
        videoView.setOnCompletionListener {
            btnComenzar.visibility = View.VISIBLE
            videoProgressBar.progress = 100
        }

        // Si el usuario hace clic sobre el video, este se pausa y aparece el botón "Comenzar"
        videoView.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnComenzar.visibility = View.VISIBLE
            }
        }

        videoView.start()

        // Tarea periódica para actualizar la barra de progreso del video
        handler.removeCallbacks(updateProgressRunnable)
        handler.post(updateProgressRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Pausar/detener el video y limpiar callbacks al salir de la pantalla principal
        handler.removeCallbacks(updateProgressRunnable)
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.stopPlayback()
        }
    }
}
