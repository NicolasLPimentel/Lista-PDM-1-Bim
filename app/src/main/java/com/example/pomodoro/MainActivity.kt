package com.example.pomodoro

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

@Suppress("SpellCheckingInspection")
class MainActivity : AppCompatActivity() {
    private lateinit var tempoText : TextView
    private lateinit var inputMin : EditText
    private lateinit var inputSeg : EditText
    private lateinit var botaoIniciar : Button
    private var contagemRegressiva : CountDownTimer ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tempoText = findViewById<TextView>(R.id.tempoText)
        inputMin = findViewById<EditText>(R.id.inputMin)
        inputSeg = findViewById<EditText>(R.id.inputSeg)
        botaoIniciar = findViewById<Button>(R.id.botaoIniciar)

        botaoIniciar.setOnClickListener {
            botaoIniciar.text = getString(R.string.botao_pausar)
            inputMin.isEnabled = false
            inputSeg.isEnabled = false

            val tempoMin = inputMin.text.toString().toIntOrNull() ?:0
            val tempoSeg = inputSeg.text.toString().toIntOrNull() ?:0

            if (tempoMin > 0 || tempoSeg > 0) {
                var tempoMilliS = tempoMin * 60 * 1000L //de minutos para milissegundos
                tempoMilliS += tempoSeg * 1000L // soma os segundos para milissegundos
                iniciarTemporizador(tempoMilliS)
            }
        }
    }

    private fun iniciarTemporizador(tempoMillis : Long) {
        contagemRegressiva?.cancel()
        contagemRegressiva = object : CountDownTimer(tempoMillis, 1000) {
            override fun onTick(millisAteFim : Long) {
                val minutos = millisAteFim / 60000  //millissegundos para minutos
                val segundos = (millisAteFim % 60000) / 1000
                val locale = Locale("en", "US")
                tempoText.text = String.format(locale,"%02d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                botaoIniciar.text = getString(R.string.botao_iniciar)
                tempoText.text = getString(R.string.tempo_zero)
                iniciarVibracao()
            }
        }.start()
    }

    private fun iniciarVibracao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrar = getSystemService(VIBRATOR_SERVICE) as Vibrator
            val efeitoVibrar = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrar.vibrate(efeitoVibrar)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }
}