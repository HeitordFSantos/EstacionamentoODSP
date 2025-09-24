package com.example.estacionamentoodsp // Verifique o pacote


import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SaidaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_todos)

        dbHelper = DatabaseHelper(this)

        val botaoVoltar = findViewById<Button>(R.id.buttonVoltarListaTodos)
        val linearLayoutContainer = findViewById<LinearLayout>(R.id.linearLayoutCarrosTodos)

        botaoVoltar.setOnClickListener {
            finish()
        }

        carregarTodosOsVeiculos(linearLayoutContainer)
    }

    private fun carregarTodosOsVeiculos(container: LinearLayout) {
        val db = dbHelper.readableDatabase
        container.removeAllViews()

        val projection = arrayOf(
            DatabaseContract.PatioEntry.COLUMN_NAME_PLACA,
            DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA
        )

        // Busca sem filtro para trazer todos os registros
        val cursor: Cursor = db.query(
            DatabaseContract.PatioEntry.TABLE_NAME,
            projection, null, null, null, null, null
        )

        if (cursor.count == 0) {
            val textViewVazio = TextView(this)
            textViewVazio.text = getString(R.string.nenhum_registro)
            container.addView(textViewVazio)
        } else {
            while (cursor.moveToNext()) {
                val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_PLACA))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO))
                val dataEntrada = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA))
                val dataSaida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA))

                val cardCarro = TextView(this)
                cardCarro.textSize = 16f
                cardCarro.setPadding(0, 16, 0, 16)
                cardCarro.text = "PLACA: $placa\n" +
                        "DONO: $nome\n" +
                        "ENTRADA: $dataEntrada\n" +
                        "SAÍDA: ${if (dataSaida.isNullOrEmpty()) "Ainda no pátio" else dataSaida}\n" +
                        "-----------------------------"
                container.addView(cardCarro)
            }
        }
        cursor.close()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}