package com.example.estacionamentoodsp

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ListaTodosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_todos)

        dbHelper = DatabaseHelper(this)

        // Encontrar os componentes da interface
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltarListaTodos)
        val linearLayoutContainer = findViewById<LinearLayout>(R.id.linearLayoutCarrosTodos)

        // Configurar o clique do botão Voltar
        botaoVoltar.setOnClickListener {
            finish()
        }

        // Chamar a função que carrega e exibe os dados
        carregarTodosOsVeiculos(linearLayoutContainer)
    }

    private fun carregarTodosOsVeiculos(container: LinearLayout) {
        val db = dbHelper.readableDatabase
        container.removeAllViews() // Limpa a lista antes de adicionar os novos itens

        // Colunas que queremos buscar no banco de dados
        val projection = arrayOf(
            DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO
        )

        // Busca sem filtro para trazer todos os registros
        val cursor: Cursor = db.query(
            DatabaseContract.VeiculoEntry.TABLE_NAME,
            projection, null, null, null, null, null
        )

        if (cursor.count == 0) {
            // Se não houver nenhum registro, exibe a mensagem de pátio vazio
            val textViewVazio = TextView(this)
            textViewVazio.text = getString(R.string.nenhum_registro)
            container.addView(textViewVazio)
        } else {
            // Se houver registros, percorre cada um deles
            while (cursor.moveToNext()) {
                val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO))
                val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE))
                val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO))

                // Cria um "card" de texto para cada veículo
                val cardCarro = TextView(this)
                cardCarro.textSize = 16f
                cardCarro.setPadding(0, 16, 0, 16)

                // Monta o texto para exibição
                cardCarro.text = "PLACA: $placa\n" +
                        "DONO: $nome\n" +
                        "TELEFONE: $telefone\n" +
                        "MODELO: ${if (modelo.isNullOrEmpty()) "Não informado" else modelo}\n" +
                        "-----------------------------"

                // Adiciona o card de texto ao contêiner na tela
                container.addView(cardCarro)
            }
        }
        // É crucial fechar o cursor para liberar os recursos
        cursor.close()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}