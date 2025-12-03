package com.example.appderotas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ManipulacaoDoBancoDeDados {
    private SQLiteDatabase escrever;
    private SQLiteDatabase le;
    private GerenciadorBanco gerenciadorBanco;

    public ManipulacaoDoBancoDeDados(Context context){
        gerenciadorBanco = new GerenciadorBanco(context);
        escrever = gerenciadorBanco.getWritableDatabase();
        le = gerenciadorBanco.getReadableDatabase();
    }

    // tela de configurações
    public boolean salvarConfiguracaoUsuario(double peso, double altura , String sexo , String tipoMapa , String modoNavegacao ){
        ContentValues cv = new ContentValues();
        cv.put("peso" , peso );
        cv.put("altura" , altura);
        cv.put("sexo", sexo);
        cv.put("tipo_mapa", tipoMapa);
        cv.put("modo_navegacao", modoNavegacao);

        try {
            // atualiza o id do usuario se não tiver cria um.
            int rows = escrever.update("Usuario" , cv , "id=?" , new String[]{"1"});
            if(rows == 0){
                escrever.insert("usuario" , null, cv);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
    //metodos para salvar as trilhas

    // Iniciar uma trilha
    public long iniciarTrilha(String nome, String horaInicio ){
        ContentValues cv = new ContentValues();
        cv.put("nome", nome);
        cv.put("data_inicio" , horaInicio);

        return escrever.insert("trilhas", null , cv);
    }
    public void SalvarPonto(long idTrilha , double latitude , double longitude, double altitude , String hora){
        ContentValues cv = new ContentValues();
        cv.put("id_trilha", idTrilha);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("altitude", altitude);
        cv.put("momento", hora);

        escrever.insert("pontos_trilhas" , null , cv );
    }

    // salva ponto no gps
    public  void salvarTrilhaCompleta(String nome, String dataInicio, String dataFim, double distancia ,
                                      double calorias, double velMedia, double velMax , ArrayList<LatLng> listaDepontos){

        long idTrilha = iniciarTrilha(nome, dataInicio);

        for (LatLng ponto : listaDepontos ) {
            SalvarPonto( idTrilha , ponto.latitude , ponto.longitude,0.0 , " ");
        }
        finalizarTrilha(idTrilha, dataFim, distancia, calorias, velMedia, velMax);
    }
    public void finalizarTrilha(long idTrilha , String horaFim, double distancia, double calorias , double velMedia , double velMax  ){
        ContentValues cv = new ContentValues();
        cv.put("data_fim" , horaFim);
        cv.put("distancia_total" , distancia );
        cv.put("gasto_calorico", calorias);
        cv.put("velocidade_media" , velMedia);
        cv.put("velocidade_maxima", velMax);

        String[] ars = {String.valueOf(idTrilha)};
        escrever.update("trilhas" , cv , "id=?", ars);
    }

    // metodos para consultar as trilas
    public Cursor listarTodasTrilhas() {
        // TRUQUE: "id AS _id" faz o Android achar que a coluna se chama _id
        String sql = "SELECT id as _id, * FROM trilhas ORDER BY id DESC";
        return le.rawQuery(sql, null);
    }


    // Busca os pontos de uma trilha específica para desenhar no mapa (Consultar Trilha)
    public Cursor listarPontosDaTrilha(long idTrilha) {
        String sql = "SELECT * FROM pontos_trilhas WHERE id_trilha = ?";
        String[] args = {String.valueOf(idTrilha)};
        return le.rawQuery(sql, args);

    }
    public void excluirTrilha(long idTrilha) {
        String[] args = {String.valueOf(idTrilha)};
        escrever.delete("pontos_trilhas", "id_trilha=?", args);
        escrever.delete("trilhas", "id=?", args);
    }

    public Cursor buscarTrilhaPeloId(long idTrilha) {
        String sql = "SELECT * FROM trilhas WHERE id = ?";
        String[] args = {String.valueOf(idTrilha)};
        return le.rawQuery(sql, args);
    }

}