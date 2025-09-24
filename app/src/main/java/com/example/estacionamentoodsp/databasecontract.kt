package com.example.estacionamentoodsp


import android.provider.BaseColumns // <<-- ESTA LINHA É CRUCIAL E PROVAVELMENTE A CAUSA DO ERRO

object DatabaseContract {

    /**
     * Tabela responsável por armazenar os dados "master" dos veículos. Aqui ficam apenas
     * as informações de cadastro (placa, contato e modelo), sem relação com o movimento
     * do pátio.
     */
    object VeiculoEntry : BaseColumns {
        const val TABLE_NAME = "veiculos_master"
        const val COLUMN_NAME_PLACA = "placa"
        const val COLUMN_NAME_NOME_DONO = "nome_dono"
        const val COLUMN_NAME_TELEFONE = "telefone"
        const val COLUMN_NAME_MODELO = "modelo"
    }

    /**
     * Tabela que representa os registros de veículos presentes no pátio. Cada linha
     * referencia um cadastro master e mantém os dados de entrada e saída.
     */
    object PatioEntry : BaseColumns {
        const val TABLE_NAME = "veiculos_no_patio"
        const val COLUMN_NAME_VEICULO_ID = "veiculo_id"
        const val COLUMN_NAME_PLACA = "placa"
        const val COLUMN_NAME_NOME_DONO = "nome_dono"
        const val COLUMN_NAME_TELEFONE = "telefone"
        const val COLUMN_NAME_MODELO = "modelo"
        const val COLUMN_NAME_DATA_ENTRADA = "data_entrada"
        const val COLUMN_NAME_DATA_SAIDA = "data_saida"
    }
}
