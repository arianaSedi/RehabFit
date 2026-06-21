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
    private Button btnContinuar;
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
        btnContinuar = findViewById(R.id.btnContinuar);

        //obtiene las tarjetas que representan las zonas del cuerpo
        MaterialCardView cardBrazo = findViewById(R.id.cardBrazo);
        MaterialCardView cardHombro = findViewById(R.id.cardHombro);
        MaterialCardView cardMuneca = findViewById(R.id.cardMuneca);
        MaterialCardView cardMano = findViewById(R.id.cardMano);
        MaterialCardView cardEspalda = findViewById(R.id.cardEspalda);
        MaterialCardView cardRodilla = findViewById(R.id.cardRodilla);
        MaterialCardView cardPierna = findViewById(R.id.cardPierna);
        MaterialCardView cardTobillo = findViewById(R.id.cardTobillo);

        //asigna una zona especifica a cada tarjeta
        configurarCard(cardBrazo, "Brazo");
        configurarCard(cardHombro, "Hombro");
        configurarCard(cardMuneca, "Muñeca");
        configurarCard(cardMano, "Mano");
        configurarCard(cardEspalda, "Espalda");
        configurarCard(cardRodilla, "Rodilla");
        configurarCard(cardPierna, "Pierna");
        configurarCard(cardTobillo, "Tobillo");

        limpiarSeleccion();

        //actualiza el valor del dolor cuando el usuario mueve la barra
        seekDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // muestra el nivel de dolor seleccionado
                txtDolor.setText(progress + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnContinuar.setOnClickListener(v -> guardarDatos());
    }

    //metodo que configura el comportamiento de cada CARD
    private void configurarCard(MaterialCardView card, String zona) {

        card.setOnClickListener(v -> {

            limpiarSeleccion();

            // resalta visualmente la tarjeta seleccionada
            card.setStrokeColor(getResources().getColor(R.color.verde_principal));
            card.setCardBackgroundColor(getResources().getColor(R.color.verde_claro));
            zonaSeleccionada = zona;
        });
    }

    private void limpiarSeleccion() {

        int[] cards = {R.id.cardBrazo, R.id.cardHombro, R.id.cardMuneca, R.id.cardMano, R.id.cardEspalda, R.id.cardRodilla, R.id.cardPierna, R.id.cardTobillo};

        for (int id : cards) {
            MaterialCardView card = findViewById(id);

            // restaura los colores originales de cada tarjeta
            card.setCardBackgroundColor(getResources().getColor(R.color.blanco));
            card.setStrokeColor(getResources().getColor(R.color.borde));
        }
    }

    //guarda la zona afectada y el nivel de dolor en firebase
    private void guardarDatos() {

        if (zonaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una zona", Toast.LENGTH_SHORT).show();
            return;
        }

        //verifica que exista una sesion iniciada
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // obtiene el identificador unico del usuario
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // guarda la zona afectada en el perfil del usuario
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .child("zonaAfectada")
                .setValue(zonaSeleccionada);

        // guarda el nivel de dolor seleccionado
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