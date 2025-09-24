package com.example.estacionamentoodsp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "Estacionamento.db"

        private const val SQL_CREATE_VEICULOS_MASTER =
            "CREATE TABLE ${DatabaseContract.VeiculoEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_PLACA} TEXT NOT NULL UNIQUE," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_NOME_DONO} TEXT NOT NULL," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_TELEFONE} TEXT NOT NULL," +
                    "${DatabaseContract.VeiculoEntry.COLUMN_NAME_MODELO} TEXT)"

        private const val SQL_CREATE_VEICULOS_PATIO =
            "CREATE TABLE ${DatabaseContract.PatioEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_VEICULO_ID} INTEGER," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_PLACA} TEXT NOT NULL," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_NOME_DONO} TEXT NOT NULL," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_TELEFONE} TEXT NOT NULL," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_MODELO} TEXT," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_ENTRADA} TEXT," +
                    "${DatabaseContract.PatioEntry.COLUMN_NAME_DATA_SAIDA} TEXT," +
                    "FOREIGN KEY(${DatabaseContract.PatioEntry.COLUMN_NAME_VEICULO_ID}) REFERENCES " +
                    "${DatabaseContract.VeiculoEntry.TABLE_NAME}(${BaseColumns._ID}) ON DELETE SET NULL)"

        private const val SQL_DELETE_VEICULOS_MASTER =
            "DROP TABLE IF EXISTS ${DatabaseContract.VeiculoEntry.TABLE_NAME}"

        private const val SQL_DELETE_VEICULOS_PATIO =
            "DROP TABLE IF EXISTS ${DatabaseContract.PatioEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_VEICULOS_MASTER)
        db.execSQL(SQL_CREATE_VEICULOS_PATIO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_VEICULOS_PATIO)
        db.execSQL(SQL_DELETE_VEICULOS_MASTER)
        onCreate(db)
    }
}
