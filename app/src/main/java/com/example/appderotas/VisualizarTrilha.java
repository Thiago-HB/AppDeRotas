package com.example.appderotas;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; // Importante para o tema
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class VisualizarTrilha extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ManipulacaoDoBancoDeDados db;
    private long idTrilha;
    private TextView txtTitulo, txtDetalhes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_trilha); // Tem que ser o mesmo nome do XML acima

        // Recupera ID
        idTrilha = getIntent().getLongExtra("ID_TRILHA", -1);

        db = new ManipulacaoDoBancoDeDados(this);

        // VINCULAÇÃO DOS COMPONENTES (Ids novos do XML acima)
        txtTitulo = findViewById(R.id.tvTituloFinal);
        txtDetalhes = findViewById(R.id.tvDetalhesFinal);

        // Inicializa o Mapa (Modo Clássico)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaFinal);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Erro fatal: Mapa não carregou.", Toast.LENGTH_LONG).show();
        }

        carregarDadosTexto();
    }

    private void carregarDadosTexto() {
        if (idTrilha == -1) {
            txtTitulo.setText("Erro de ID");
            return;
        }

        try {
            Cursor c = db.buscarTrilhaPeloId(idTrilha);
            if (c != null && c.moveToFirst()) {
                // Tenta pegar os índices de forma segura
                int idxNome = c.getColumnIndex("nome");
                int idxDist = c.getColumnIndex("distancia_total");
                int idxVel = c.getColumnIndex("velocidade_maxima");
                int idxCal = c.getColumnIndex("gasto_calorico");

                // Verifica se as colunas existem (-1 significa que não achou)
                if (idxNome == -1 || idxDist == -1) {
                    txtTitulo.setText("Banco de Dados Desatualizado");
                    txtDetalhes.setText("Reinstale o app para corrigir as tabelas.");
                    return;
                }

                String nome = c.getString(idxNome);
                double dist = c.getDouble(idxDist);
                double velMax = c.getDouble(idxVel);
                double cal = c.getDouble(idxCal);

                txtTitulo.setText(nome);
                txtDetalhes.setText(String.format("Distância: %.2f m\nVel. Máx: %.1f km/h\nCalorias: %.0f kcal", dist, velMax, cal));
            } else {
                txtTitulo.setText("Trilha não encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostra o erro no Logcat
            txtTitulo.setText("Erro ao ler banco");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        desenharTrilha();
    }

    private void desenharTrilha() {
        try {
            Cursor cursorPontos = db.listarPontosDaTrilha(idTrilha);

            if (cursorPontos == null || cursorPontos.getCount() == 0) {
                return;
            }

            List<LatLng> pontos = new ArrayList<>();
            PolylineOptions linha = new PolylineOptions().width(12).color(Color.BLUE);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // Pega índices das colunas de latitude/longitude
            int idxLat = cursorPontos.getColumnIndex("latitude");
            int idxLon = cursorPontos.getColumnIndex("longitude");

            if (idxLat == -1 || idxLon == -1) return;

            while (cursorPontos.moveToNext()) {
                double lat = cursorPontos.getDouble(idxLat);
                double lon = cursorPontos.getDouble(idxLon);
                LatLng ponto = new LatLng(lat, lon);
                pontos.add(ponto);
                linha.add(ponto);
                builder.include(ponto);
            }

            mMap.addPolyline(linha);

            if (!pontos.isEmpty()) {
                mMap.addMarker(new MarkerOptions().position(pontos.get(0)).title("Início"));
                mMap.addMarker(new MarkerOptions().position(pontos.get(pontos.size() - 1)).title("Fim"));

                // Move a câmera (try-catch para evitar crash de tamanho do mapa)
                try {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                } catch (Exception e) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontos.get(0), 15));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao desenhar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}