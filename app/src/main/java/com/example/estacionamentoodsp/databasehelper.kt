package com.example.estacionamentoodsp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Estacionamento.db"

        // Comando SQL para criar nossa tabela com as colunas do Contrato
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${DatabaseContract.VeiculoEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," + // ID único para cada entrada
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA} TEXT NOT NULL," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO} TEXT NOT NULL," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE} TEXT NOT NULL," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO} TEXT," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_DATA_ENTRADA} TEXT," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_DATA_SAIDA} TEXT)"

        // Comando SQL para apagar a tabela, caso precisemos atualizar a estrutura
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DatabaseContract.VeiculoEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}