package com.example.estacionamentoodsp

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaidaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var linearLayoutContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saida)

        dbHelper = DatabaseHelper(this)
        linearLayoutContainer = findViewById(R.id.linearLayoutCarrosSaida)
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltarMenu)

        botaoVoltar.setOnClickListener { finish() }

        // Carrega a lista automaticamente ao abrir a tela
        carregarCarrosNoPatio()
    }

    private fun carregarCarrosNoPatio() {
        val db = dbHelper.readableDatabase
        linearLayoutContainer.removeAllViews()

        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseContract.PatioEntry.COLUMN_NAME_PLACA,
            DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.PatioEntry.COLUMN_NAME_TELEFONE,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA
        )

        val cursor = db.query(
            DatabaseContract.PatioEntry.TABLE_NAME,
            projection,
            "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA} IS NULL",
            null,
            null, null, "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} DESC"
        )

        if (cursor.count == 0) {
            val vazio = TextView(this)
            vazio.text = "Nenhum veículo no pátio."
            vazio.gravity = android.view.Gravity.CENTER
            vazio.setPadding(0, 50, 0, 0)
            linearLayoutContainer.addView(vazio)
        } else {
            val inflater = LayoutInflater.from(this)
            
            while (cursor.moveToNext()) {
                val idRegistro = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_PLACA))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO))
                val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_TELEFONE))
                val entrada = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA))

                // Inflar o card para cada veículo
                val cardView = inflater.inflate(R.layout.item_veiculo_patio, linearLayoutContainer, false)
                
                val tvPlaca = cardView.findViewById<TextView>(R.id.textViewPlacaCard)
                val tvDono = cardView.findViewById<TextView>(R.id.textViewDonoCard)
                val tvEntrada = cardView.findViewById<TextView>(R.id.textViewEntradaCard)
                val btnWhatsapp = cardView.findViewById<Button>(R.id.buttonWhatsappCard)
                val btnSair = cardView.findViewById<Button>(R.id.buttonSairCard)

                tvPlaca.text = placa
                tvDono.text = "Dono: $nome"
                tvEntrada.text = "Entrada: $entrada"

                // Lógica do botão WhatsApp
                btnWhatsapp.setOnClickListener {
                    abrirWhatsapp(telefone, placa)
                }

                // Lógica do botão Retirar
                btnSair.setOnClickListener {
                    confirmarSaida(idRegistro, placa)
                }

                linearLayoutContainer.addView(cardView)
            }
        }
        cursor.close()
    }

    private fun confirmarSaida(idRegistro: Long, placa: String) {
        val db = dbHelper.writableDatabase
        val dataSaida = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA, dataSaida)
        }

        val rowsUpdated = db.update(
            DatabaseContract.PatioEntry.TABLE_NAME,
            values,
            "${BaseColumns._ID} = ?",
            arrayOf(idRegistro.toString())
        )

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Saída registrada para $placa!", Toast.LENGTH_SHORT).show()
            carregarCarrosNoPatio() // Atualiza a lista
        } else {
            Toast.makeText(this, "Erro ao registrar saída.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirWhatsapp(telefone: String, placa: String) {
        try {
            // Remove caracteres não numéricos do telefone
            val numeroLimpo = telefone.replace(Regex("[^0-9]"), "")
            // Adiciona o código do país se não houver (exemplo Brasil: 55)
            val numeroFinal = if (numeroLimpo.length <= 11) "55$numeroLimpo" else numeroLimpo
            
            val mensagem = "Olá, estamos entrando em contato sobre o veículo de placa $placa que está em nosso estacionamento."
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$numeroFinal&text=${Uri.encode(mensagem)}")
            
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
