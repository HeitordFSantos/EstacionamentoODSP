package com.example.estacionamentoodsp


import android.provider.BaseColumns // <<-- ESTA LINHA É CRUCIAL E PROVAVELMENTE A CAUSA DO ERRO

object DatabaseContract {

    // Objeto que define a estrutura da nossa tabela de veículos
    // A herança ": BaseColumns" nos dá acesso ao campo _ID
    object VeiculoEntry : BaseColumns {
        const val TABLE_NAME = "veiculos_no_patio"
        const val COLUMN_NAME_PLACA = "placa"
        const val COLUMN_NAME_NOME_DONO = "nome_dono"
        const val COLUMN_NAME_TELEFONE = "telefone"
        const val COLUMN_NAME_MODELO = "modelo"
        const val COLUMN_NAME_DATA_ENTRADA = "data_entrada"
        const val COLUMN_NAME_DATA_SAIDA = "data_saida"
    }
}