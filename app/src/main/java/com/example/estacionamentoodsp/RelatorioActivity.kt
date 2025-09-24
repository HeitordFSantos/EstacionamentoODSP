package com.example.estacionamentoodsp

import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class RelatorioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var textViewResultado: TextView
    private lateinit var editTextPeriodo: EditText
    private lateinit var radioGroupTipoRelatorio: RadioGroup

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            gerarPdf(uri)
        } else {
            Toast.makeText(this, "Criação de PDF cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorio)

        dbHelper = DatabaseHelper(this)

        // Encontrar os componentes, incluindo os novos
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltarRelatorio)
        val botaoGerarRelatorio = findViewById<Button>(R.id.buttonGerarRelatorio)
        val botaoExportarPdf = findViewById<Button>(R.id.buttonExportarPdf)
        editTextPeriodo = findViewById(R.id.editTextPeriodo)
        textViewResultado = findViewById(R.id.textViewResultadoRelatorio)
        radioGroupTipoRelatorio = findViewById(R.id.radioGroupTipoRelatorio)

        // --- NOVA LÓGICA PARA MUDAR A DICA DO CAMPO DE TEXTO ---
        radioGroupTipoRelatorio.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButtonDiario -> editTextPeriodo.hint = getString(R.string.hint_data_relatorio)
                R.id.radioButtonMensal -> editTextPeriodo.hint = getString(R.string.hint_periodo_mensal)
                R.id.radioButtonAnual -> editTextPeriodo.hint = getString(R.string.hint_periodo_anual)
            }
        }

        botaoVoltar.setOnClickListener {
            finish()
        }

        botaoGerarRelatorio.setOnClickListener {
            val periodo = editTextPeriodo.text.toString()
            if (periodo.isNotEmpty()) {
                // Descobre qual RadioButton está selecionado
                val tipoSelecionadoId = radioGroupTipoRelatorio.checkedRadioButtonId
                val tipoSelecionado = findViewById<RadioButton>(tipoSelecionadoId)

                // Gera o relatório com base no tipo selecionado
                gerarRelatorio(periodo, tipoSelecionado.text.toString(), textViewResultado)
            } else {
                Toast.makeText(this, "Por favor, digite um período.", Toast.LENGTH_SHORT).show()
            }
        }

        botaoExportarPdf.setOnClickListener {
            val textoDoRelatorio = textViewResultado.text.toString()
            if (textoDoRelatorio != getString(R.string.nenhum_registro) && textoDoRelatorio.isNotEmpty()) {
                val nomeArquivo = "relatorio_${editTextPeriodo.text.toString().replace('/', '-')}.pdf"
                createFileLauncher.launch(nomeArquivo)
            } else {
                Toast.makeText(this, "Gere um relatório primeiro para poder exportar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gerarRelatorio(periodo: String, tipo: String, textView: TextView) {
        val db = dbHelper.readableDatabase
        val stringBuilder = StringBuilder()

        val projection = arrayOf(
            DatabaseContract.PatioEntry.COLUMN_NAME_PLACA,
            DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA
        )

        // --- LÓGICA DE BUSCA DINÂMICA ---
        val selection = "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} LIKE ?"
        val selectionArgs = when (tipo) {
            getString(R.string.tipo_relatorio_mensal) -> arrayOf("%$periodo%") // Busca por MM/AAAA
            getString(R.string.tipo_relatorio_anual) -> arrayOf("%$periodo%")   // Busca por AAAA
            else -> arrayOf("$periodo%") // Padrão (Diário): busca por DD/MM/AAAA no início
        }

        val cursor: Cursor = db.query(
            DatabaseContract.PatioEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.count == 0) {
            textView.text = getString(R.string.nenhum_registro)
        } else {
            // A lógica para exibir o resultado continua a mesma
            while (cursor.moveToNext()) {
                val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_PLACA))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO))
                val dataEntrada = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA))
                val dataSaida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA)) ?: "Ainda no pátio"
                stringBuilder.append("PLACA: $placa\n")
                stringBuilder.append("DONO: $nome\n")
                stringBuilder.append("ENTRADA: $dataEntrada\n")
                stringBuilder.append("SAÍDA: ${if (dataSaida.isEmpty()) "Ainda no pátio" else dataSaida}\n")
                stringBuilder.append("-----------------------------\n\n")
            }
            textView.text = stringBuilder.toString()
        }
        cursor.close()
    }

    // A função gerarPdf continua a mesma
    private fun gerarPdf(uri: Uri) {
        try {
            val textoDoRelatorio = textViewResultado.text.toString()
            val outputStream = contentResolver.openOutputStream(uri)
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            document.add(Paragraph(textoDoRelatorio))
            document.close()
            Toast.makeText(this, "PDF salvo com sucesso!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}