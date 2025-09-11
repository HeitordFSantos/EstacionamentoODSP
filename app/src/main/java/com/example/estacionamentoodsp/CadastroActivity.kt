package com.example.estacionamentoodsp

import android.content.ContentValues
import android.content.Intent // Import necessário
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.provider.BaseColumns

class CadastroActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var veiculoIdParaEdicao: Long = -1

    // Declarar os componentes da interface
    private lateinit var buttonVerTodos: Button // NOVO
    private lateinit var editTextBuscaPlaca: EditText
    private lateinit var buttonBuscar: Button
    private lateinit var layoutDadosVeiculo: LinearLayout
    private lateinit var editTextNomeEdicao: EditText
    private lateinit var editTextTelefoneEdicao: EditText
    private lateinit var editTextModeloEdicao: EditText
    private lateinit var buttonAlterar: Button
    private lateinit var buttonExcluir: Button
    private lateinit var buttonVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        dbHelper = DatabaseHelper(this)

        // Vincular os componentes
        buttonVerTodos = findViewById(R.id.buttonlistarTodos) // NOVO
        editTextBuscaPlaca = findViewById(R.id.editTextBuscaPlaca)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        layoutDadosVeiculo = findViewById(R.id.layoutDadosVeiculo)
        editTextNomeEdicao = findViewById(R.id.editTextNomeEdicao)
        editTextTelefoneEdicao = findViewById(R.id.editTextTelefoneEdicao)
        editTextModeloEdicao = findViewById(R.id.editTextModeloEdicao)
        buttonAlterar = findViewById(R.id.buttonAlterar)
        buttonExcluir = findViewById(R.id.buttonExcluir)
        buttonVoltar = findViewById(R.id.buttonVoltarCadastro)

        // Configurar os cliques dos botões
        buttonVerTodos.setOnClickListener {
            startActivity(Intent(this, ListaTodosActivity::class.java))
        }
        buttonBuscar.setOnClickListener { buscarVeiculo() }
        buttonAlterar.setOnClickListener { alterarVeiculo() }
        buttonExcluir.setOnClickListener { confirmarExclusao() }
        buttonVoltar.setOnClickListener { finish() }
    }

    // As funções buscarVeiculo(), alterarVeiculo(), confirmarExclusao(), excluirVeiculo() e onDestroy() continuam as mesmas
    private fun buscarVeiculo() {
        val placa = editTextBuscaPlaca.text.toString()
        if (placa.isEmpty()) {
            Toast.makeText(this, "Por favor, digite uma placa.", Toast.LENGTH_SHORT).show()
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
        val sortOrder = "${BaseColumns._ID} DESC"
        val cursor = db.query(
            DatabaseContract.VeiculoEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder,
            "1"
        )
        if (cursor.count > 0) {
            cursor.moveToFirst()
            veiculoIdParaEdicao = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO))
            val telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE))
            val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO))

            editTextNomeEdicao.setText(nome)
            editTextTelefoneEdicao.setText(telefone)
            editTextModeloEdicao.setText(modelo)
            layoutDadosVeiculo.visibility = View.VISIBLE
            Toast.makeText(this, "Veículo encontrado. Pode editar os dados.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.veiculo_nao_encontrado), Toast.LENGTH_SHORT).show()
            layoutDadosVeiculo.visibility = View.GONE
        }
        cursor.close()
    }

    private fun alterarVeiculo() {
        val nome = editTextNomeEdicao.text.toString()
        val telefone = editTextTelefoneEdicao.text.toString()
        val modelo = editTextModeloEdicao.text.toString()

        if (nome.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Nome e Telefone são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO, nome)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE, telefone)
            put(DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO, modelo)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(veiculoIdParaEdicao.toString())
        val count = db.update(DatabaseContract.VeiculoEntry.TABLE_NAME, values, selection, selectionArgs)
        if (count > 0) {
            Toast.makeText(this, getString(R.string.dados_alterados_sucesso), Toast.LENGTH_SHORT).show()
            layoutDadosVeiculo.visibility = View.GONE
            editTextBuscaPlaca.text.clear()
        } else {
            Toast.makeText(this, getString(R.string.erro_alterar_dados), Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza de que deseja excluir este registro? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim, Excluir") { dialog, which ->
                excluirVeiculo()
            }
            .setNegativeButton("Não, Cancelar", null)
            .show()
    }

    private fun excluirVeiculo() {
        val db = dbHelper.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(veiculoIdParaEdicao.toString())
        val count = db.delete(DatabaseContract.VeiculoEntry.TABLE_NAME, selection, selectionArgs)
        if (count > 0) {
            Toast.makeText(this, getString(R.string.registro_excluido_sucesso), Toast.LENGTH_SHORT).show()
            layoutDadosVeiculo.visibility = View.GONE
            editTextBuscaPlaca.text.clear()
        } else {
            Toast.makeText(this, getString(R.string.erro_excluir_registro), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}