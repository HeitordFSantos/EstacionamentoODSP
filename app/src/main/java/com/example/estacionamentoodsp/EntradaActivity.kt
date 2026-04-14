package com.example.estacionamentoodsp

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntradaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var layoutCadastroRapido: LinearLayout
    private lateinit var editTextNomeNovo: EditText
    private lateinit var editTextTelefoneNovo: EditText
    private lateinit var editTextModeloNovo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada)

        dbHelper = DatabaseHelper(this)

        val editTextPlaca = findViewById<EditText>(R.id.editTextPlacaEntrada)
        val botaoDarEntrada = findViewById<Button>(R.id.buttonDarEntrada)
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltar)
        
        layoutCadastroRapido = findViewById(R.id.layoutCadastroRapido)
        editTextNomeNovo = findViewById(R.id.editTextNomeNovo)
        editTextTelefoneNovo = findViewById(R.id.editTextTelefoneNovo)
        editTextModeloNovo = findViewById(R.id.editTextModeloNovo)

        botaoVoltar.setOnClickListener { finish() }

        botaoDarEntrada.setOnClickListener {
            val placa = editTextPlaca.text.toString().trim().uppercase()

            if (placa.isEmpty()) {
                Toast.makeText(this, "Digite a placa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase

            // 1. Verifica se o veículo existe no cadastro master
            val cursor = db.query(
                DatabaseContract.VeiculoEntry.TABLE_NAME,
                arrayOf(
                    BaseColumns._ID,
                    DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO,
                    DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE,
                    DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO
                ),
                "${DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA} = ?",
                arrayOf(placa),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                // VEÍCULO JÁ CADASTRADO
                val veiculoId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO))
                val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE))
                val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO)) ?: ""
                cursor.close()

                layoutCadastroRapido.visibility = View.GONE
                registrarEntradaNoPatio(veiculoId, placa, nome, telefone, modelo)
            } else {
                // VEÍCULO NÃO CADASTRADO
                cursor.close()
                
                if (layoutCadastroRapido.visibility == View.GONE) {
                    // Primeira vez: mostra os campos para preencher
                    layoutCadastroRapido.visibility = View.VISIBLE
                    Toast.makeText(this, "Veículo novo! Preencha os dados para cadastrar e dar entrada.", Toast.LENGTH_LONG).show()
                } else {
                    // Segunda vez: tenta cadastrar e dar entrada
                    val nome = editTextNomeNovo.text.toString().trim()
                    val telefone = editTextTelefoneNovo.text.toString().trim()
                    val modelo = editTextModeloNovo.text.toString().trim()

                    if (nome.isEmpty() || telefone.isEmpty() || modelo.isEmpty()) {
                        Toast.makeText(this, "Por favor, preencha todos os campos do veículo.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Realiza o cadastro master e a entrada simultaneamente
                        val novoVeiculoId = cadastrarVeiculoMaster(placa, nome, telefone, modelo)
                        if (novoVeiculoId != -1L) {
                            registrarEntradaNoPatio(novoVeiculoId, placa, nome, telefone, modelo)
                        } else {
                            Toast.makeText(this, "Erro ao cadastrar veículo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun cadastrarVeiculoMaster(placa: String, nome: String, telefone: String, modelo: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA, placa)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO, nome)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE, telefone)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO, modelo)
        }
        return db.insert(DatabaseContract.VeiculoEntry.TABLE_NAME, null, values)
    }

    private fun registrarEntradaNoPatio(veiculoId: Long, placa: String, nome: String, telefone: String, modelo: String) {
        val db = dbHelper.writableDatabase

        // Verifica se já está no pátio
        val cursorPatio = db.query(
            DatabaseContract.PatioEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID),
            "${DatabaseContract.PatioEntry.COLUMN_NAME_PLACA} = ? AND ${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA} IS NULL",
            arrayOf(placa),
            null, null, null
        )

        if (cursorPatio.moveToFirst()) {
            Toast.makeText(this, "Este veículo já está no pátio!", Toast.LENGTH_LONG).show()
            cursorPatio.close()
        } else {
            cursorPatio.close()
            val dataEntrada = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            val values = ContentValues().apply {
                put(DatabaseContract.PatioEntry.COLUMN_NAME_VEICULO_ID, veiculoId)
                put(DatabaseContract.PatioEntry.COLUMN_NAME_PLACA, placa)
                put(DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO, nome)
                put(DatabaseContract.PatioEntry.COLUMN_NAME_TELEFONE, telefone)
                put(DatabaseContract.PatioEntry.COLUMN_NAME_MODELO, modelo)
                put(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA, dataEntrada)
            }

            val newRowId = db.insert(DatabaseContract.PatioEntry.TABLE_NAME, null, values)
            if (newRowId != -1L) {
                Toast.makeText(this, "Entrada registrada com sucesso!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao registrar entrada no pátio.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
