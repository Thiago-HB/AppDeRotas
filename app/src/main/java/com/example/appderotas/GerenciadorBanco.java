package com.example.appderotas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GerenciadorBanco extends SQLiteOpenHelper {
    private static final String NomeBanco = "bancoDeDados";
    // Mudei para versão 2 para garantir que a atualização ocorra
    private static final int Versao_Banco = 2;

    public GerenciadorBanco(Context context){
        super(context , NomeBanco, null ,Versao_Banco);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela Usuario
        db.execSQL("CREATE TABLE usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, peso REAL, altura REAL, sexo TEXT," +
                " data_nascimento TEXT, tipo_mapa TEXT, modo_navegacao TEXT)");

        // Tabela Trilhas (Adicionei as colunas que faltavam para não dar erro no finalizarTrilha)
        db.execSQL("CREATE TABLE trilhas (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, data_inicio TEXT, " +
                "data_fim TEXT, distancia_total REAL, gasto_calorico REAL, velocidade_media REAL, velocidade_maxima REAL)");

        // Tabela Pontos
        db.execSQL("CREATE TABLE pontos_trilhas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_trilha INTEGER, latitude REAL, longitude REAL," +
                " altitude REAL, momento TEXT, FOREIGN KEY(id_trilha) REFERENCES trilhas(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se a versão mudou, apaga tudo e cria de novo (Cuidado: apaga dados antigos)
        db.execSQL("DROP TABLE IF EXISTS pontos_trilhas");
        db.execSQL("DROP TABLE IF EXISTS trilhas");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        onCreate(db);
    }
}
