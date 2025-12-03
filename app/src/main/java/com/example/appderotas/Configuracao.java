package com.example.appderotas;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Configuracao extends AppCompatActivity {

    private ManipulacaoDoBancoDeDados db;

    // Componentes da tela
    private EditText editPeso, editAltura, editDataNasc;
    private RadioGroup rgSexo, rgMapa, rgNavegacao;
    private Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao); // Nome do seu XML de config

        db = new ManipulacaoDoBancoDeDados(this);

        // 1. Vincular os componentes (Certifique-se que esses IDs existem no seu XML)
        editPeso = findViewById(R.id.Peso); // ID que estava no seu XML

        // ATENÇÃO: Adicione esses IDs no seu XML se não tiverem
        editAltura = findViewById(R.id.altura); // O ID confuso que estava no seu XML

        // Crie botões de salvar no XML se não tiver
        btnSalvar = findViewById(R.id.btnSalvarConfig);

        // Ação do Botão Salvar
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarDados();
            }
        });
    }

    private void salvarDados() {
        try {
            double peso = Double.parseDouble(editPeso.getText().toString());
            double altura = Double.parseDouble(editAltura.getText().toString());

            // Valores padrão (Mockados) caso você não tenha feito os RadioButtons no XML ainda
            String sexo = "M";
            String tipoMapa = "VETORIAL";
            String modoNav = "NORTH_UP";



            if (rgSexo.getCheckedRadioButtonId() != -1) {
                RadioButton rb = findViewById(rgSexo.getCheckedRadioButtonId());
                sexo = rb.getText().toString();
            }


            boolean sucesso = db.salvarConfiguracaoUsuario(peso, altura, sexo, tipoMapa, modoNav);

            if (sucesso) {
                Toast.makeText(this, "Configurações Salvas!", Toast.LENGTH_SHORT).show();
                finish(); // Fecha a tela
            } else {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Preencha Peso e Altura corretamente", Toast.LENGTH_SHORT).show();
        }
    }
}