package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rehabfit.models.PerfilAdaptado;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PerfilAdaptadoActivity extends AppCompatActivity {

    private EditText edtEdad;
    private Spinner spNivelMovilidad, spObjetivoPrincipal;
    private RadioGroup rgApoyoFisico;
    private SeekBar seekDolor;
    private TextView txtValorDolor;
    private AppCompatButton btnGuardarPerfil;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    private int nivelDolor = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_adaptado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        edtEdad = findViewById(R.id.edtEdad);
        spNivelMovilidad = findViewById(R.id.spNivelMovilidad);
        spObjetivoPrincipal = findViewById(R.id.spObjetivoPrincipal);
        rgApoyoFisico = findViewById(R.id.rgApoyoFisico);
        seekDolor = findViewById(R.id.seekDolor);
        txtValorDolor = findViewById(R.id.txtValorDolor);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);

        cargarSpinners();

        seekDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nivelDolor = progress;
                txtValorDolor.setText(nivelDolor + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No se usa
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No se usa
            }
        });

        btnGuardarPerfil.setOnClickListener(v -> guardarPerfil());
    }

    private void cargarSpinners() {

        ArrayList<String> nivelesMovilidad = new ArrayList<>();
        nivelesMovilidad.add("Selecciona tu nivel");
        nivelesMovilidad.add("Baja movilidad");
        nivelesMovilidad.add("Movilidad moderada");
        nivelesMovilidad.add("Buena movilidad");
        nivelesMovilidad.add("Movilidad alta");

        ArrayAdapter<String> adapterMovilidad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nivelesMovilidad
        );

        adapterMovilidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNivelMovilidad.setAdapter(adapterMovilidad);

        ArrayList<String> objetivos = new ArrayList<>();
        objetivos.add("Selecciona tu objetivo");
        objetivos.add("Reducir dolor");
        objetivos.add("Mejorar movilidad");
        objetivos.add("Fortalecer músculos");
        objetivos.add("Recuperación física");
        objetivos.add("Mejorar equilibrio");

        ArrayAdapter<String> adapterObjetivos = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                objetivos
        );

        adapterObjetivos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spObjetivoPrincipal.setAdapter(adapterObjetivos);
    }

    private void guardarPerfil() {
        String edadTexto = edtEdad.getText().toString().trim();

        if (TextUtils.isEmpty(edadTexto)) {
            edtEdad.setError("Ingresa tu edad");
            return;
        }

        int edad;

        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            edtEdad.setError("Ingresa una edad válida");
            return;
        }

        if (edad <= 0 || edad > 120) {
            edtEdad.setError("Ingresa una edad válida");
            return;
        }

        if (spNivelMovilidad.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tu nivel de movilidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spObjetivoPrincipal.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tu objetivo principal", Toast.LENGTH_SHORT).show();
            return;
        }

        int radioSeleccionado = rgApoyoFisico.getCheckedRadioButtonId();

        if (radioSeleccionado == -1) {
            Toast.makeText(this, "Selecciona el apoyo físico que utilizas", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rbSeleccionado = findViewById(radioSeleccionado);

        String apoyoFisico = rbSeleccionado.getText().toString();
        String nivelMovilidad = spNivelMovilidad.getSelectedItem().toString();
        String objetivoPrincipal = spObjetivoPrincipal.getSelectedItem().toString();

        FirebaseUser usuarioActual = auth.getCurrentUser();

        if (usuarioActual == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(PerfilAdaptadoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String uid = usuarioActual.getUid();

        PerfilAdaptado perfil = new PerfilAdaptado(edad, nivelMovilidad, apoyoFisico, objetivoPrincipal, nivelDolor, true);

        usuariosRef.child(uid).child("perfilAdaptado").setValue(perfil)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PerfilAdaptadoActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
