package com.example.juegotriviav1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.juegotriviav1.data.ScoreManager

/**
 * Fragment reutilizable que muestra el Top 3 de mejores puntajes
 * con sus respectivas medallas, puntajes, tiempos y fechas.
 */
class RankingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val containerItems = view.findViewById<LinearLayout>(R.id.containerRankingItems)
        val topScores = ScoreManager(requireContext()).getTopScores()

        containerItems.removeAllViews()

        if (topScores.isEmpty()) {
            val tvEmpty = TextView(requireContext()).apply {
                text = "Aún no hay puntajes registrados."
                setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
                textSize = 14f
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(0, 16, 0, 16)
            }
            containerItems.addView(tvEmpty)
        } else {
            topScores.forEachIndexed { index, record ->
                val tvRecord = TextView(requireContext()).apply {
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "•"
                    }
                    text = "$medal ${record.score}/5 — ${record.timeSeconds}s (${record.dateTime})"
                    setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                }
                containerItems.addView(tvRecord)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos si la pantalla vuelve a primer plano
        view?.let { onViewCreated(it, arguments) }
    }
}
