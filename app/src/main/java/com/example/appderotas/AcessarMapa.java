package com.example.appderotas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AcessarMapa extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private ManipulacaoDoBancoDeDados db; // Sua classe existente

    // Controle da Trilha
    private ArrayList<LatLng> listaDePontos;
    private LocationManager locationManager;
    private boolean isTracking = false;
    private long idTrilhaAtual = -1;

    // Dados estatísticos
    private double distanciaTotal = 0.0;
    private double velocidadeMaxima = 0.0;
    private double velocidadeAtual = 0.0;
    private double caloriasQueimadas = 0.0;
    private Location localizacaoAnterior = null;

    // Cronômetro
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    // Interface (IDs do XML novo que te passei)
    private TextView txtCronometro, txtVelocidade, txtDistancia, txtVelMax, txtCalorias;
    private Button btnParar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Garanta que o nome do XML aqui é o mesmo do arquivo que te mandei com o painel de dados
        setContentView(R.layout.activity_acessar_mapa);

        db = new ManipulacaoDoBancoDeDados(this);
        listaDePontos = new ArrayList<>();

        // Vincula os IDs do XML
        txtCronometro = findViewById(R.id.txtCronometro);
        txtVelocidade = findViewById(R.id.txtVelocidadeAtual);
        txtDistancia = findViewById(R.id.txtDistancia);
        txtVelMax = findViewById(R.id.txtVelocidadeMax);
        txtCalorias = findViewById(R.id.txtCalorias);
        btnParar = findViewById(R.id.btnPararTrilha);

        // Ação do botão parar
        btnParar.setOnClickListener(v -> pararESalvarTrilha());

        // Carrega o Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ConfiguracaoDeMapa config = new ConfiguracaoDeMapa(mMap);
        config.ativarInterface(); // Seus métodos de config
        config.mudarSatelete();

        // Tenta iniciar a trilha assim que o mapa carrega
        iniciarTrilha();
    }

    private void iniciarTrilha() {
        // Verifica permissão de GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (isTracking) return;
        isTracking = true;

        String dataInicio = Calendar.getInstance().getTime().toString();

        // Usa seu método iniciarTrilha que retorna o ID (long)
        idTrilhaAtual = db.iniciarTrilha("Trilha " + dataInicio, dataInicio);

        // Inicia Cronômetro
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        // Inicia GPS (Min: 2 segundos ou 3 metros)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, this);

        // Ativa a bolinha azul de localização do Google
        mMap.setMyLocationEnabled(true);

        Toast.makeText(this, "Trilha iniciada!", Toast.LENGTH_SHORT).show();
    }

    private void pararESalvarTrilha() {
        // Verificação de segurança: Se não estava gravando, não faz nada
        if (!isTracking) return;

        // --- PASSO 1: PARAR SENSORES ---
        if (locationManager != null) {
            locationManager.removeUpdates(this); // Para de usar o GPS
        }
        customHandler.removeCallbacks(updateTimerThread); // Para o Cronômetro
        isTracking = false;

        // --- PASSO 2: PREPARAR DADOS FINAIS ---
        String dataFim = Calendar.getInstance().getTime().toString();

        // Cálculo da Velocidade Média Final (km/h)
        // Evita divisão por zero se o tempo for muito curto
        double segundosTotais = updatedTime / 1000.0;
        double velocidadeMediaFinal = 0.0;
        if (segundosTotais > 0) {
            velocidadeMediaFinal = (distanciaTotal / segundosTotais) * 3.6;
        }

        // --- PASSO 3: SALVAR NO BANCO ---
        // Chama o método finalizarTrilha da sua classe ManipulacaoDoBancoDeDados
        // ATENÇÃO: Se o seu banco estiver desatualizado (sem as colunas novas), o app TRAVA aqui.
        try {
            db.finalizarTrilha(
                    idTrilhaAtual,      // ID da trilha que abrimos no início (onCreate)
                    dataFim,            // Hora que acabou
                    distanciaTotal,     // Distância acumulada
                    caloriasQueimadas,  // Calorias calculadas
                    velocidadeMediaFinal,
                    velocidadeMaxima
            );

            Toast.makeText(this, "Trilha salva com sucesso!", Toast.LENGTH_LONG).show();

            // --- PASSO 4: FECHAR ---
            finish(); // Fecha a Activity e volta para o Menu

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!isTracking) return;

        LatLng pontoAtual = new LatLng(location.getLatitude(), location.getLongitude());
        listaDePontos.add(pontoAtual);

        // Salva ponto no banco (usando seu método SalvarPonto)
        db.SalvarPonto(idTrilhaAtual, location.getLatitude(), location.getLongitude(), location.getAltitude(), Calendar.getInstance().getTime().toString());

        // Desenha linha no mapa
        if (listaDePontos.size() > 1) {
            LatLng pontoAnterior = listaDePontos.get(listaDePontos.size() - 2);
            mMap.addPolyline(new PolylineOptions()
                    .add(pontoAnterior, pontoAtual)
                    .width(10)
                    .color(Color.BLUE));

            // Cálculos
            float[] results = new float[1];
            Location.distanceBetween(pontoAnterior.latitude, pontoAnterior.longitude,
                    pontoAtual.latitude, pontoAtual.longitude, results);
            double distanciaTrecho = results[0];
            distanciaTotal += distanciaTrecho;

            // Velocidade
            velocidadeAtual = location.hasSpeed() ? location.getSpeed() * 3.6 : 0.0;
            if (velocidadeAtual > velocidadeMaxima) velocidadeMaxima = velocidadeAtual;

            // Calorias (Ex: 70kg * dist(km) * 1.03) - Simplificado
            caloriasQueimadas += (distanciaTrecho / 1000.0) * 70.0 * 1.03;
        }

        // Move a câmera seguindo o usuário
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pontoAtual, 18f));

        atualizarTextoUI();
    }

    private void atualizarTextoUI() {
        txtDistancia.setText(String.format(Locale.getDefault(), "%.2f km", distanciaTotal / 1000));
        txtVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeAtual));
        txtVelMax.setText(String.format(Locale.getDefault(), "Máx: %.1f", velocidadeMaxima));
        txtCalorias.setText(String.format(Locale.getDefault(), "%.0f kcal", caloriasQueimadas));
    }

    // Lógica do Cronômetro
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hrs = mins / 60;
            secs = secs % 60;
            mins = mins % 60;
            txtCronometro.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs));
            customHandler.postDelayed(this, 1000);
        }
    };

    // Métodos obrigatórios vazios
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}
}