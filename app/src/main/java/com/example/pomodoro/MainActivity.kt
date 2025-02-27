@file:Suppress("SpellCheckingInspection")

package com.example.pomodoro

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tela: View
    private lateinit var interrupcoes: TextView
    private lateinit var estadoText: TextView
    private lateinit var tempoText: TextView
    private lateinit var inputMin: EditText
    private lateinit var inputSeg: EditText
    private lateinit var botaoPrincipal: Button
    private var contagemRegressiva: CountDownTimer? = null
    private var tempoRestante: Long = 0
    private var estaRodando = false
    private var interrupcoesNum = 0
    private var ciclosCompletos = 0
    private var estadoAtual = Estado.Foco
    private var aguardandoDescanso = false
    private var pausadoDuranteDescanso = false

    private enum class Estado {
        Foco, DescansoCurto, DescansoLongo
    }

    private val tempoDescansoCurto = 5 * 60 * 1000L
    private val tempoDescansoLongo = 15 * 60 * 1000L

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tela = findViewById(R.id.main)
        estadoText = findViewById(R.id.estadoText)
        tempoText = findViewById(R.id.tempoText)
        inputMin = findViewById(R.id.inputMin)
        inputSeg = findViewById(R.id.inputSeg)
        botaoPrincipal = findViewById(R.id.botaoIniciar)
        interrupcoes = findViewById(R.id.interrupcoes)
        estadoText.visibility = View.INVISIBLE

        botaoPrincipal.setOnClickListener {
            when {
                aguardandoDescanso -> iniciarDescanso()
                estaRodando -> {
                    interrupcoesNum++
                    interrupcoes.text = "Interrupções totais: $interrupcoesNum"
                    pausarTemporizador()
                }
                else -> {
                    if (tempoRestante > 0) {
                        retomarTemporizador()
                    } else {
                        iniciarFoco()
                    }
                }
            }
        }
    }

    private fun iniciarFoco() {
        val minutos = inputMin.text.toString().toIntOrNull() ?: 0
        val segundos = inputSeg.text.toString().toIntOrNull() ?: 0
        val tempoFoco = (minutos * 60 + segundos) * 1000L

        if (tempoFoco <= 0) {
            Toast.makeText(this, "Digite um tempo válido!", Toast.LENGTH_SHORT).show()
            return
        }

        estadoAtual = Estado.Foco
        pausadoDuranteDescanso = false
        tela.setBackgroundColor(Color.parseColor("#FFB3B3"))
        estadoText.text = getString(R.string.estado_foco)
        iniciarTemporizador(tempoFoco)
    }

    private fun iniciarTemporizador(tempoMillis: Long) {
        estadoText.visibility = View.VISIBLE
        contagemRegressiva?.cancel()

        contagemRegressiva = object : CountDownTimer(tempoMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tempoRestante = millisUntilFinished
                estaRodando = true
                val minutos = millisUntilFinished / 60000
                val segundos = (millisUntilFinished % 60000) / 1000
                tempoText.text = String.format(Locale.US, "%02d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                tempoRestante = 0
                estaRodando = false
                iniciarVibracao()

                if (estadoAtual == Estado.Foco) {
                    prepararParaDescanso()
                } else {
                    finalizarDescanso()
                }
            }
        }.start()

        inputMin.isEnabled = false
        inputSeg.isEnabled = false
        botaoPrincipal.text = getString(R.string.botao_pausar)
    }

    private fun retomarTemporizador() {
        iniciarTemporizador(tempoRestante)
    }

    private fun pausarTemporizador() {
        contagemRegressiva?.cancel()
        estaRodando = false
        pausadoDuranteDescanso = estadoAtual != Estado.Foco
        botaoPrincipal.text = getString(R.string.botao_retomar)
    }

    @Suppress("DEPRECATION")
    private fun iniciarVibracao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrador = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrador.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun prepararParaDescanso() {
        aguardandoDescanso = true
        botaoPrincipal.text = getString(R.string.botao_descansar)
    }

    private fun iniciarDescanso() {
        aguardandoDescanso = false
        ciclosCompletos++
        val tempoDescanso = if (ciclosCompletos % 4 == 0) tempoDescansoLongo else tempoDescansoCurto

        estadoAtual = if (ciclosCompletos % 4 == 0) Estado.DescansoLongo else Estado.DescansoCurto
        tela.setBackgroundColor(if (estadoAtual == Estado.DescansoLongo) Color.parseColor("#B3FFB3") else Color.parseColor("#B3D9FF"))
        estadoText.text = if (estadoAtual == Estado.DescansoLongo) getString(R.string.estado_DL) else getString(R.string.estado_DC)

        iniciarTemporizador(tempoDescanso)
        botaoPrincipal.text = getString(R.string.botao_pausar)
    }

    private fun finalizarDescanso() {
        // Habilitar os campos de entrada
        inputMin.isEnabled = true
        inputSeg.isEnabled = true

        // Resetar o botão para "Iniciar"
        botaoPrincipal.text = getString(R.string.botao_iniciar)

        // Resetar estado para permitir novo ciclo de foco
        estadoAtual = Estado.Foco
        tela.setBackgroundColor(Color.parseColor("#FFFFFF")) // Cor neutra
        estadoText.visibility = View.INVISIBLE
    }
}
