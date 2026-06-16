package com.example.rehabfit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ZonaAfectadaActivity extends AppCompatActivity {

    private String zonaSeleccionada = "";

    private TextView txtDolor;
    private SeekBar seekDolor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_zona_afectada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtDolor = findViewById(R.id.txtDolor);
        seekDolor = findViewById(R.id.seekDolor);

        Button btnContinuar = findViewById(R.id.btnContinuar);

        MaterialCardView cardBrazo = findViewById(R.id.cardBrazo);
        MaterialCardView cardHombro = findViewById(R.id.cardHombro);
        MaterialCardView cardMuneca = findViewById(R.id.cardMuneca);
        MaterialCardView cardMano = findViewById(R.id.cardMano);
        MaterialCardView cardEspalda = findViewById(R.id.cardEspalda);
        MaterialCardView cardRodilla = findViewById(R.id.cardRodilla);
        MaterialCardView cardPierna = findViewById(R.id.cardPierna);
        MaterialCardView cardTobillo = findViewById(R.id.cardTobillo);

        configurarCard(cardBrazo, "Brazo");
        configurarCard(cardHombro, "Hombro");
        configurarCard(cardMuneca, "Muñeca");
        configurarCard(cardMano, "Mano");
        configurarCard(cardEspalda, "Espalda");
        configurarCard(cardRodilla, "Rodilla");
        configurarCard(cardPierna, "Pierna");
        configurarCard(cardTobillo, "Tobillo");

        limpiarSeleccion();

        seekDolor.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtDolor.setText(progress + "/10");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });

        btnContinuar.setOnClickListener(v -> guardarDatos());
    }

    private void configurarCard(MaterialCardView card, String zona) {

        card.setOnClickListener(v -> {

            limpiarSeleccion();
            card.setStrokeColor(getResources().getColor(R.color.verde_principal));
            card.setCardBackgroundColor(getResources().getColor(R.color.verde_claro));

            zonaSeleccionada = zona;
        });
    }

    private void limpiarSeleccion() {

        int[] cards = {
                R.id.cardBrazo,
                R.id.cardHombro,
                R.id.cardMuneca,
                R.id.cardMano,
                R.id.cardEspalda,
                R.id.cardRodilla,
                R.id.cardPierna,
                R.id.cardTobillo
        };

        for (int id : cards) {

            MaterialCardView card = findViewById(id);
            card.setCardBackgroundColor(getResources().getColor(R.color.blanco));
            card.setStrokeColor(getResources().getColor(R.color.borde));
        }
    }

    private void guardarDatos() {

        if (zonaSeleccionada.isEmpty()) {

            Toast.makeText(this, "Selecciona una zona", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .child("zonaAfectada")
                .setValue(zonaSeleccionada);

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .child("nivelDolor")
                .setValue(seekDolor.getProgress());

        startActivity(new Intent(ZonaAfectadaActivity.this, MainActivity.class));
        finish();
    }
}