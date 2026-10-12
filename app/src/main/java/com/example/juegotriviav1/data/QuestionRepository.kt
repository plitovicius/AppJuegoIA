package com.example.juegotriviav1.data

import android.content.Context
import com.example.juegotriviav1.model.Question
import org.json.JSONArray
import java.nio.charset.Charset

/**
 * Repositorio encargado de cargar y parsear las preguntas de la trivia desde un archivo JSON
 * ubicado en la carpeta assets del proyecto.
 */
object QuestionRepository {

    /**
     * Carga el archivo "questions.json" desde los assets y lo convierte en una lista de objetos [Question].
     * @param context Contexto de la aplicación para acceder a los assets.
     * @return Lista de preguntas parseadas.
     */
    fun loadQuestions(context: Context): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            val inputStream = context.assets.open("questions.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charset.forName("UTF-8"))
            val jsonArray = JSONArray(jsonStr)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optionsArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }

                questions.add(
                    Question(
                        id = obj.getInt("id"),
                        question = obj.getString("question"),
                        options = options,
                        correctIndex = obj.getInt("correctIndex"),
                        explanation = obj.getString("explanation"),
                        headerColor = obj.getString("headerColor"),
                        backgroundColor = obj.getString("backgroundColor")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }
}
