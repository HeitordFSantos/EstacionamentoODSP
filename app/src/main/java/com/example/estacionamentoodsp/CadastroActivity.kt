package com.example.estacionamentoodsp

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.provider.BaseColumns
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class CadastroActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var veiculoIdParaEdicao: Long = -1

    // Componentes
    private lateinit var buttonVerTodos: Button
    private lateinit var editTextBuscaPlaca: EditText
    private lateinit var buttonBuscar: Button
    private lateinit var cardDadosVeiculo: MaterialCardView
    private lateinit var editTextNomeEdicao: TextInputEditText
    private lateinit var editTextTelefoneEdicao: TextInputEditText
    private lateinit var editTextModeloEdicao: TextInputEditText
    private lateinit var buttonSalvar: Button
    private lateinit var buttonExcluir: Button
    private lateinit var buttonVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        dbHelper = DatabaseHelper(this)

        // Vincular os componentes
        buttonVerTodos = findViewById(R.id.buttonlistarTodos)
        editTextBuscaPlaca = findViewById(R.id.editTextBuscaPlaca)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        cardDadosVeiculo = findViewById(R.id.cardDadosVeiculo)
        editTextNomeEdicao = findViewById(R.id.editTextNomeEdicao)
        editTextTelefoneEdicao = findViewById(R.id.editTextTelefoneEdicao)
        editTextModeloEdicao = findViewById(R.id.editTextModeloEdicao)
        buttonSalvar = findViewById(R.id.buttonAlterar)
        buttonExcluir = findViewById(R.id.buttonExcluir)
        buttonVoltar = findViewById(R.id.buttonVoltarCadastro)

        // Configurar botões
        buttonVerTodos.setOnClickListener {
            startActivity(Intent(this, ListaTodosActivity::class.java))
        }
        buttonBuscar.setOnClickListener { buscarVeiculo() }
        buttonSalvar.setOnClickListener { salvarVeiculo() }
        buttonExcluir.setOnClickListener { confirmarExclusao() }
        buttonVoltar.setOnClickListener { finish() }
    }

    private fun buscarVeiculo() {
        val placa = editTextBuscaPlaca.text.toString().trim().uppercase()
        if (placa.isEmpty()) {
            Toast.makeText(this, "Digite a placa.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE,
            DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO
        )
        val selection = "${DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA} = ?"
        val selectionArgs = arrayOf(placa)

        val cursor = db.query(
            DatabaseContract.VeiculoEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null, null, null
        )

        if (cursor.moveToFirst()) {
            veiculoIdParaEdicao = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO))
            val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE))
            val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO))

            editTextNomeEdicao.setText(nome)
            editTextTelefoneEdicao.setText(telefone)
            editTextModeloEdicao.setText(modelo)
            
            cardDadosVeiculo.visibility = View.VISIBLE
            buttonSalvar.text = "Salvar Alterações"
            Toast.makeText(this, "Veículo encontrado.", Toast.LENGTH_SHORT).show()
        } else {
            veiculoIdParaEdicao = -1L
            editTextNomeEdicao.text?.clear()
            editTextTelefoneEdicao.text?.clear()
            editTextModeloEdicao.text?.clear()
            
            cardDadosVeiculo.visibility = View.VISIBLE
            buttonSalvar.text = "Cadastrar Novo"
            Toast.makeText(this, "Placa não cadastrada. Preencha para criar.", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    private fun salvarVeiculo() {
        val nome = editTextNomeEdicao.text.toString().trim()
        val telefone = editTextTelefoneEdicao.text.toString().trim()
        val modelo = editTextModeloEdicao.text.toString().trim()
        val placa = editTextBuscaPlaca.text.toString().trim().uppercase()

        if (nome.isEmpty() || telefone.isEmpty() || placa.isEmpty()) {
            Toast.makeText(this, "Placa, Nome e Telefone são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA, placa)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO, nome)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE, telefone)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO, modelo)
        }

        if (veiculoIdParaEdicao == -1L) {
            val newId = db.insert(DatabaseContract.VeiculoEntry.TABLE_NAME, null, values)
            if (newId != -1L) {
                Toast.makeText(this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                cardDadosVeiculo.visibility = View.GONE
                editTextBuscaPlaca.text.clear()
            }
        } else {
            val selection = "${BaseColumns._ID} = ?"
            val selectionArgs = arrayOf(veiculoIdParaEdicao.toString())
            val count = db.update(DatabaseContract.VeiculoEntry.TABLE_NAME, values, selection, selectionArgs)
            if (count > 0) {
                Toast.makeText(this, "Cadastro atualizado!", Toast.LENGTH_SHORT).show()
                cardDadosVeiculo.visibility = View.GONE
                editTextBuscaPlaca.text.clear()
            }
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir veículo")
            .setMessage("Deseja realmente excluir este cadastro?")
            .setPositiveButton("Sim") { _, _ -> excluirVeiculo() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun excluirVeiculo() {
        if (veiculoIdParaEdicao == -1L) return

        val db = dbHelper.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(veiculoIdParaEdicao.toString())
        val count = db.delete(DatabaseContract.VeiculoEntry.TABLE_NAME, selection, selectionArgs)

        if (count > 0) {
            Toast.makeText(this, "Cadastro excluído.", Toast.LENGTH_SHORT).show()
            cardDadosVeiculo.visibility = View.GONE
            editTextBuscaPlaca.text.clear()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
