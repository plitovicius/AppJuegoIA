package com.example.juegotriviav1.model

/**
 * Modelo de datos que representa una pregunta de la trivia.
 * Contiene el enunciado, las opciones de respuesta, el índice de la respuesta correcta,
 * la explicación pedagógica y los colores de diseño para la interfaz.
 */
data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val headerColor: String,
    val backgroundColor: String
)
