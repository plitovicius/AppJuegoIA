package com.example.juegotriviav1.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ScoreRecord(
    val score: Int,
    val timeSeconds: Int,
    val dateTime: String
)

class ScoreManager(context: Context) {
    private val prefs = context.getSharedPreferences("TriviaRankingPrefs", Context.MODE_PRIVATE)

    fun saveScore(score: Int, timeSeconds: Int, dateTime: String) {
        val newRecord = ScoreRecord(score, timeSeconds, dateTime)

        // 1. Guardar en el historial completo (añadir al final o al inicio)
        val historyList = getAllScores().toMutableList()
        historyList.add(0, newRecord) // Las más recientes primero
        saveListToPrefs("ALL_SCORES_JSON", historyList)

        // 2. Actualizar el Top 3 (ordenado por puntaje DESC, luego tiempo ASC, máximo 3)
        val topList = getTopScores().toMutableList()
        topList.add(newRecord)
        val sortedTop = topList.sortedWith(
            compareByDescending<ScoreRecord> { it.score }
                .thenBy { it.timeSeconds }
        ).take(3)
        saveListToPrefs("TOP_SCORES_JSON", sortedTop)
    }

    fun getTopScores(): List<ScoreRecord> {
        return getListFromPrefs("TOP_SCORES_JSON")
    }

    fun getAllScores(): List<ScoreRecord> {
        return getListFromPrefs("ALL_SCORES_JSON")
    }

    private fun getListFromPrefs(key: String): List<ScoreRecord> {
        val jsonStr = prefs.getString(key, "[]") ?: "[]"
        val list = mutableListOf<ScoreRecord>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ScoreRecord(
                        score = obj.getInt("score"),
                        timeSeconds = obj.getInt("timeSeconds"),
                        dateTime = obj.getString("dateTime")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveListToPrefs(key: String, list: List<ScoreRecord>) {
        val jsonArray = JSONArray()
        for (record in list) {
            val obj = JSONObject().apply {
                put("score", record.score)
                put("timeSeconds", record.timeSeconds)
                put("dateTime", record.dateTime)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(key, jsonArray.toString()).apply()
    }
}
