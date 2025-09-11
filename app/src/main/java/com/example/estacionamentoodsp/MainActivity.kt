package com.example.estacionamentoodsp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Encontra TODOS os botões primeiro
        val botaoEntrada = findViewById<Button>(R.id.buttonEntrada)
        val botaoSaida = findViewById<Button>(R.id.buttonSaida)
        val botaoRelatorio = findViewById<Button>(R.id.buttonRelatorio)
        val botaoGerenciar = findViewById<Button>(R.id.buttonCadastro)

        // --- Configura a ação de CADA botão de forma independente ---

        // Ação do Botão Entrada
        botaoEntrada.setOnClickListener {
            val intent = Intent(this, EntradaActivity::class.java)
            startActivity(intent)
        }

        // Ação do Botão Saída
        botaoSaida.setOnClickListener {
            val intent = Intent(this, SaidaActivity::class.java)
            startActivity(intent)
        }

        // Ação do Botão Relatório
        botaoRelatorio.setOnClickListener {
            val intent = Intent(this, RelatorioActivity::class.java)
            startActivity(intent)
        }

        // Ação do Botão Gerenciar
        botaoGerenciar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }
}


