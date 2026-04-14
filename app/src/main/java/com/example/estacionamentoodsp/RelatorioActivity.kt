package com.example.estacionamentoodsp

import android.app.DatePickerDialog
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.card.MaterialCardView
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.io.font.constants.StandardFonts
import java.io.OutputStream
import java.util.*

class RelatorioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var textViewResultado: TextView
    private lateinit var editTextPeriodo: EditText
    private lateinit var radioGroupTipoRelatorio: RadioGroup
    private lateinit var textViewTotalVeiculos: TextView
    private lateinit var cardResumo: MaterialCardView

    private val createPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) gerarPdf(uri)
    }

    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) exportarParaCsv(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorio)

        dbHelper = DatabaseHelper(this)

        val botaoVoltar = findViewById<Button>(R.id.buttonVoltarRelatorio)
        val botaoGerarRelatorio = findViewById<Button>(R.id.buttonGerarRelatorio)
        val botaoExportarPdf = findViewById<Button>(R.id.buttonExportarPdf)
        val botaoExportarCsv = findViewById<Button>(R.id.buttonExportarCsv)

        editTextPeriodo = findViewById(R.id.editTextPeriodo)
        textViewResultado = findViewById(R.id.textViewResultadoRelatorio)
        radioGroupTipoRelatorio = findViewById(R.id.radioGroupTipoRelatorio)
        textViewTotalVeiculos = findViewById(R.id.textViewTotalVeiculos)
        cardResumo = findViewById(R.id.cardResumo)

        editTextPeriodo.setOnClickListener {
            mostrarDatePicker()
        }

        radioGroupTipoRelatorio.setOnCheckedChangeListener { _, _ ->
            editTextPeriodo.setText("")
            cardResumo.visibility = View.GONE
            textViewResultado.text = "Os detalhes aparecerão aqui..."
        }

        botaoVoltar.setOnClickListener { finish() }

        botaoGerarRelatorio.setOnClickListener {
            val periodo = editTextPeriodo.text.toString()
            if (periodo.isNotEmpty()) {
                val tipoSelecionadoId = radioGroupTipoRelatorio.checkedRadioButtonId
                val tipoSelecionado = findViewById<RadioButton>(tipoSelecionadoId)
                gerarRelatorio(periodo, tipoSelecionado.text.toString())
            } else {
                Toast.makeText(this, "Por favor, selecione um período.", Toast.LENGTH_SHORT).show()
            }
        }

        botaoExportarPdf.setOnClickListener {
            if (validarExportacao()) {
                val nomeArquivo = "relatorio_${editTextPeriodo.text}.pdf"
                createPdfLauncher.launch(nomeArquivo)
            }
        }

        botaoExportarCsv.setOnClickListener {
            if (validarExportacao()) {
                val nomeArquivo = "relatorio_${editTextPeriodo.text}.csv"
                createCsvLauncher.launch(nomeArquivo)
            }
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val tipoId = radioGroupTipoRelatorio.checkedRadioButtonId
            val periodoFormatado = when (tipoId) {
                R.id.radioButtonDiario -> String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                R.id.radioButtonMensal -> String.format("%02d/%d", selectedMonth + 1, selectedYear)
                R.id.radioButtonAnual -> selectedYear.toString()
                else -> ""
            }
            editTextPeriodo.setText(periodoFormatado)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun validarExportacao(): Boolean {
        val texto = textViewResultado.text.toString()
        if (texto.isEmpty() || texto.contains("Os detalhes aparecerão aqui") || texto == getString(R.string.nenhum_registro)) {
            Toast.makeText(this, "Gere um relatório antes de exportar.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun gerarRelatorio(periodo: String, tipo: String) {
        val db = dbHelper.readableDatabase
        val stringBuilder = StringBuilder()

        val projection = arrayOf(
            DatabaseContract.PatioEntry.COLUMN_NAME_PLACA,
            DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO,
            DatabaseContract.PatioEntry.COLUMN_NAME_TELEFONE,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA,
            DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA
        )

        val selection = "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} LIKE ?"
        val selectionArgs = when (radioGroupTipoRelatorio.checkedRadioButtonId) {
            R.id.radioButtonDiario -> arrayOf("$periodo %")
            R.id.radioButtonMensal -> arrayOf("%/$periodo %")
            R.id.radioButtonAnual -> arrayOf("%/$periodo %")
            else -> arrayOf("$periodo%")
        }

        val cursor: Cursor = db.query(
            DatabaseContract.PatioEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} ASC"
        )

        val total = cursor.count
        textViewTotalVeiculos.text = total.toString()
        cardResumo.visibility = View.VISIBLE

        if (total == 0) {
            textViewResultado.text = getString(R.string.nenhum_registro)
        } else {
            while (cursor.moveToNext()) {
                val placa = cursor.getString(0)
                val nome = cursor.getString(1)
                val entrada = cursor.getString(3)
                val saida = cursor.getString(4) ?: "Ainda no pátio"
                
                stringBuilder.append("📍 PLACA: $placa\n👤 DONO: $nome\n📅 ENTRADA: $entrada\n🏁 SAÍDA: $saida\n")
                stringBuilder.append("─────────────────────────────\n\n")
            }
            textViewResultado.text = stringBuilder.toString()
        }
        cursor.close()
    }

    private fun gerarPdf(uri: Uri) {
        try {
            val texto = textViewResultado.text.toString()
            val outputStream = contentResolver.openOutputStream(uri)
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            val fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            
            document.add(Paragraph("RELATÓRIO DE MOVIMENTAÇÃO - ESTACIONAMENTO ODSP").setFont(fontBold).setFontSize(18f))
            document.add(Paragraph("Período: ${editTextPeriodo.text}"))
            document.add(Paragraph("Total de Veículos: ${textViewTotalVeiculos.text}\n\n"))
            document.add(Paragraph(texto))
            
            document.close()
            Toast.makeText(this, "PDF salvo com sucesso!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportarParaCsv(uri: Uri) {
        try {
            val db = dbHelper.readableDatabase
            val periodo = editTextPeriodo.text.toString()
            
            val selection = "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} LIKE ?"
            val selectionArgs = when (radioGroupTipoRelatorio.checkedRadioButtonId) {
                R.id.radioButtonDiario -> arrayOf("$periodo %")
                R.id.radioButtonMensal -> arrayOf("%/$periodo %")
                R.id.radioButtonAnual -> arrayOf("%/$periodo %")
                else -> arrayOf("$periodo%")
            }

            val cursor = db.query(
                DatabaseContract.PatioEntry.TABLE_NAME,
                arrayOf(
                    DatabaseContract.PatioEntry.COLUMN_NAME_PLACA,
                    DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO,
                    DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA,
                    DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA
                ),
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            val stringBuilder = StringBuilder()
            stringBuilder.append("Placa;Dono;Entrada;Saida\n")

            while (cursor.moveToNext()) {
                val placa = cursor.getString(0)
                val nome = cursor.getString(1)
                val entrada = cursor.getString(2)
                val saida = cursor.getString(3) ?: "No patio"
                stringBuilder.append("$placa;$nome;$entrada;$saida\n")
            }
            cursor.close()

            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
            outputStream?.write(stringBuilder.toString().toByteArray())
            outputStream?.close()

            Toast.makeText(this, "CSV exportado com sucesso!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
