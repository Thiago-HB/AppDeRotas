package com.example.appderotas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button BotaoAcessarMapa =(Button) findViewById(R.id.TelaInicalGogleMaps);
        BotaoAcessarMapa.setOnClickListener(this);
        Button config = (Button) findViewById(R.id.configuracoes);
        config.setOnClickListener(this);
        Button consulta = (Button) findViewById(R.id.consulta);
        consulta.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.TelaInicalGogleMaps){
            Intent intent = new Intent(MainActivity.this , AcessarMapa.class);
            startActivity(intent);

        } else if(view.getId()==R.id.configuracoes){
            Intent intent = new Intent(this, Configuracao.class);
            startActivity(intent);
        } else if(view.getId()==R.id.consulta){
            Intent intent = new Intent(MainActivity.this, Consultartrilha.class);
            startActivity(intent);
        }
    }
}