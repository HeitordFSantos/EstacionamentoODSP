package com.example.estacionamentoodsp

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ListaTodosActivity : AppCompatActivity() {

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
            DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO
        )

        val cursor: Cursor = db.query(
            DatabaseContract.VeiculoEntry.TABLE_NAME,
            projection, null, null, null, null, 
            "${DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA} ASC"
        )

        if (cursor.count == 0) {
            val textViewVazio = TextView(this)
            textViewVazio.text = "Nenhum veículo cadastrado."
            textViewVazio.gravity = android.view.Gravity.CENTER
            textViewVazio.setPadding(0, 50, 0, 0)
            container.addView(textViewVazio)
        } else {
            val inflater = LayoutInflater.from(this)
            while (cursor.moveToNext()) {
                val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO))
                val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE))
                val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO))

                val itemView = inflater.inflate(R.layout.item_veiculo_lista, container, false)
                
                val tvPlaca = itemView.findViewById<TextView>(R.id.textViewPlacaLista)
                val tvModelo = itemView.findViewById<TextView>(R.id.textViewModeloLista)
                val tvDono = itemView.findViewById<TextView>(R.id.textViewDonoLista)
                val tvTelefone = itemView.findViewById<TextView>(R.id.textViewTelefoneLista)

                tvPlaca.text = placa
                tvModelo.text = if (modelo.isNullOrEmpty()) "Modelo não informado" else modelo
                tvDono.text = "Dono: $nome"
                tvTelefone.text = "Tel: $telefone"

                container.addView(itemView)
            }
        }
        cursor.close()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
