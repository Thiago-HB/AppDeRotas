package com.example.appderotas;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// slide 9 classes GoogleMap
public class ConfiguracaoDeMapa {
    private GoogleMap mMap;

    public ConfiguracaoDeMapa(GoogleMap GoogleMap) {
        mMap = GoogleMap;
    }
    // os tipos de mapas
    public void mudarSatelete(){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }
    // as interface do com usuario(botões de zoom, bussula)
    public void ativarInterface(){
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }


    // faz um desenho em volta serve como marcador
    public void movimentacaoDeCamera(LatLng local, float zoom ){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(local)
                .zoom(zoom)
                .bearing(90)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }
    // adiciona um circulo em volta
    public void adicionarMarcadorEforma(LatLng local){
        mMap.addMarker(new MarkerOptions()
                .position(local)
                .title("local especial"));
        mMap.addCircle( new CircleOptions()
                .center(local)
                .radius(1000)
                .strokeColor(android.R.color.holo_red_dark)
                .fillColor(Color.argb(70 , 150 , 50 , 50)));
    }

    public void adicionarMarcadores(LatLng local){
        MarkerOptions opcoes = new MarkerOptions();

        opcoes.position(local);

        opcoes.title("Sydney Opera House");
        opcoes.snippet("Um dos edifícios mais famosos do mundo.");

        opcoes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        opcoes.rotation(90.0f);
        opcoes.anchor(0.5f , 0.05f);
        opcoes.flat(true);
        mMap.addMarker(opcoes);
    }



}