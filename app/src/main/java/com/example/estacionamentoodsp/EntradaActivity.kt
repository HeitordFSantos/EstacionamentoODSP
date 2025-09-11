package com.example.estacionamentoodsp

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntradaActivity : AppCompatActivity() {

    // Declara o nosso "ajudante" de banco de dados.
    // Usamos "lateinit" porque ele será inicializado depois, no onCreate.
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada)

        // Inicializa o ajudante, "contratando" o nosso construtor de banco de dados.
        dbHelper = DatabaseHelper(this)

        // --- Encontrando todos os componentes visuais da tela ---
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltar)
        val botaoConfirmar = findViewById<Button>(R.id.buttonConfirmar)
        val editTextPlaca = findViewById<EditText>(R.id.editTextPlaca)
        val editTextNomeDono = findViewById<EditText>(R.id.editTextNomeDono)
        val editTextTelefone = findViewById<EditText>(R.id.editTextTelefone)
        val editTextModelo = findViewById<EditText>(R.id.editTextModelo)

        // --- Lógica do Botão Voltar (sem alterações) ---
        botaoVoltar.setOnClickListener {
            finish()
        }

        // --- LÓGICA DO BOTÃO CONFIRMAR (MODIFICADA) ---
        botaoConfirmar.setOnClickListener {
            val placa = editTextPlaca.text.toString()
            val nomeDono = editTextNomeDono.text.toString()
            val telefone = editTextTelefone.text.toString()
            val modelo = editTextModelo.text.toString()

            // Verificação: Placa, Nome e Telefone são obrigatórios (como no seu rascunho)
            if (placa.isNotEmpty() && nomeDono.isNotEmpty() && telefone.isNotEmpty()) {
                // --- A MÁGICA DO BANCO DE DADOS ACONTECE AQUI ---

                // 1. Pegamos uma versão "escrevível" do nosso banco de dados
                val db = dbHelper.writableDatabase

                // 2. Pegamos a data e hora atuais e formatamos como texto
                val formatoData = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val dataEntrada = formatoData.format(Date())

                // 3. Criamos um "mapa" de valores, onde o nome da coluna é a chave
                val values = ContentValues().apply {
                    put(DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA, placa)
                    put(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO, nomeDono)
                    put(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE, telefone)
                    put(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO, modelo)
                    put(DatabaseContract.VeiculoEntry.COLUMN_NAME_DATA_ENTRADA, dataEntrada)
                }

                // 4. Inserimos a nova linha no banco de dados.
                // O metodo insert() retorna -1 se houver um erro.
                val newRowId = db.insert(DatabaseContract.VeiculoEntry.TABLE_NAME, null, values)

                // 5. Verificamos se a inserção deu certo e mostramos a mensagem apropriada
                if (newRowId == -1L) {
                    // MUDANÇA AQUI: Mensagem de erro mais genérica
                    Toast.makeText(this, "Erro ao registrar o veículo.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Veículo de placa $placa registrado!", Toast.LENGTH_LONG).show()
                    finish()
                }

            } else {
                Toast.makeText(this, "Por favor, preencha Placa, Nome e Telefone.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // É uma boa prática fechar a conexão com o banco de dados quando a tela é destruída
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}