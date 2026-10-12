package com.example.juegotriviav1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.juegotriviav1.data.QuestionRepository
import com.example.juegotriviav1.model.Question
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja toda la lógica de negocio, estado del juego, puntuación
 * y temporizador basado en corrutinas (que sobrevive a rotaciones de pantalla).
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Lista de preguntas cargadas desde el repositorio JSON
    private val questions: List<Question> = QuestionRepository.loadQuestions(application)

    // Índice de la pregunta actual
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    // Pregunta actual en curso
    private val _currentQuestion = MutableLiveData<Question?>()
    val currentQuestion: LiveData<Question?> = _currentQuestion

    // Puntaje acumulado
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    // Historial de resultados de cada pregunta (true = correcto, false = incorrecto/tiempo agotado)
    private val _results = MutableList(questions.size) { false }
    val results: List<Boolean> get() = _results

    // Estado indicando si la pregunta actual ya fue respondida (o expiró el tiempo)
    private val _isAnswered = MutableLiveData(false)
    val isAnswered: LiveData<Boolean> = _isAnswered

    // Indica si la respuesta seleccionada fue correcta
    private val _isCorrect = MutableLiveData(false)
    val isCorrect: LiveData<Boolean> = _isCorrect

    // Índice de la opción seleccionada por el usuario (-1 si hubo tiempo agotado)
    private val _selectedOptionIndex = MutableLiveData(-1)
    val selectedOptionIndex: LiveData<Int> = _selectedOptionIndex

    // Estado de fin de juego (todas las preguntas respondidas)
    private val _isGameOver = MutableLiveData(false)
    val isGameOver: LiveData<Boolean> = _isGameOver

    // Tiempo restante en segundos para la pregunta actual (20 segundos)
    private val _timeLeft = MutableLiveData(20L)
    val timeLeft: LiveData<Long> = _timeLeft

    // Trabajo de corrutina para el temporizador
    private var timerJob: Job? = null

    init {
        if (questions.isNotEmpty()) {
            _currentQuestion.value = questions[0]
            startTimer()
        }
    }

    /**
     * Inicia o reanuda el temporizador de 20 segundos usando corrutinas.
     * Sobrevive a cambios de configuración (rotación de pantalla).
     */
    fun startTimer() {
        if (_isAnswered.value == true) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = _timeLeft.value ?: 20L
            if (remaining <= 0) remaining = 20L
            while (remaining > 0) {
                if (_isAnswered.value == true) break
                _timeLeft.value = remaining
                delay(1000L)
                remaining--
            }
            if (_isAnswered.value != true) {
                _timeLeft.value = 0
                timeOut()
            }
        }
    }

    /**
     * Procesa la respuesta seleccionada por el usuario.
     * @param optionIndex Índice de la opción elegida.
     */
    fun submitAnswer(optionIndex: Int) {
        if (_isAnswered.value == true) return
        timerJob?.cancel()

        val question = _currentQuestion.value ?: return
        val correct = (optionIndex == question.correctIndex)

        _selectedOptionIndex.value = optionIndex
        _isCorrect.value = correct

        val index = _currentIndex.value ?: 0
        if (index < _results.size) {
            _results[index] = correct
        }

        if (correct) {
            _score.value = (_score.value ?: 0) + 1
        }

        _isAnswered.value = true
    }

    /**
     * Maneja el evento de tiempo agotado (0 segundos).
     */
    fun timeOut() {
        if (_isAnswered.value == true) return
        timerJob?.cancel()

        val index = _currentIndex.value ?: 0
        if (index < _results.size) {
            _results[index] = false
        }

        _selectedOptionIndex.value = -1
        _isCorrect.value = false
        _isAnswered.value = true
    }

    /**
     * Avanza a la siguiente pregunta de la trivia o marca el fin del juego.
     * @return true si hay más preguntas, false si el juego terminó.
     */
    fun nextQuestion(): Boolean {
        val nextIdx = (_currentIndex.value ?: 0) + 1
        if (nextIdx < questions.size) {
            _currentIndex.value = nextIdx
            _currentQuestion.value = questions[nextIdx]
            _isAnswered.value = false
            _isCorrect.value = false
            _selectedOptionIndex.value = -1
            _timeLeft.value = 20L
            startTimer()
            return true
        } else {
            _isGameOver.value = true
            return false
        }
    }

    /** Retorna el total de preguntas de la trivia. */
    fun getTotalQuestions(): Int = questions.size

    /** Retorna el puntaje final calculado directamente desde los resultados verdaderos. */
    fun getScore(): Int = _results.count { it }
}
