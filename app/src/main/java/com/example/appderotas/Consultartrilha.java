package com.example.appderotas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Consultartrilha extends AppCompatActivity {
    private ListView listView;
    private ManipulacaoDoBancoDeDados db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultartrilha);

        db = new ManipulacaoDoBancoDeDados(this);
        listView = findViewById(R.id.listaTrilhas);

        carregarLista();

        // Clique simples: Visualizar no Mapa
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // O 'id' passado aqui já é o _id do banco SQLite (chave primária)
                Intent intent = new Intent(Consultartrilha.this, VisualizarTrilha.class);
                intent.putExtra("ID_TRILHA", id);
                startActivity(intent);
            }
        });

        // Clique longo: Excluir (Requisito: Apagar trilha)
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                db.excluirTrilha(id);
                Toast.makeText(Consultartrilha.this, "Trilha apagada", Toast.LENGTH_SHORT).show();
                carregarLista(); // Atualiza a tela
                return true;
            }
        });
    }

    private void carregarLista() {
        cursor = db.listarTodasTrilhas();

        // Mapeia as colunas do banco para o layout padrão do Android (simple_list_item_2)
        // Linha 1: Nome da Trilha | Linha 2: Data de Início
        String[] from = new String[]{"nome", "data_inicio"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2, // Layout nativo do Android
                cursor,
                from,
                to,
                0
        );

        listView.setAdapter(adapter);
    }
}
