package com.example.rehabfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
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
    private RadioButton rbNinguno;
    private RadioButton rbBaston;
    private RadioButton rbMuletas;
    private RadioButton rbSillaRuedas;
    private SeekBar seekDolor;
    private TextView txtValorDolor;
    private AppCompatButton btnGuardarPerfil;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    private int nivelDolor = 5;

    private String apoyoFisicoSeleccionado = "";

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

        //inicializa firebase
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        edtEdad = findViewById(R.id.edtEdad);
        spNivelMovilidad = findViewById(R.id.spNivelMovilidad);
        spObjetivoPrincipal = findViewById(R.id.spObjetivoPrincipal);
        rbNinguno = findViewById(R.id.rbNinguno);
        rbBaston = findViewById(R.id.rbBaston);
        rbMuletas = findViewById(R.id.rbMuletas);
        rbSillaRuedas = findViewById(R.id.rbSillaRuedas);
        seekDolor = findViewById(R.id.seekDolor);
        txtValorDolor = findViewById(R.id.txtValorDolor);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);

        cargarSpinners();
        configurarRadioButtons();
        configurarSeekBar();
        btnGuardarPerfil.setOnClickListener(v -> guardarPerfil());
    }

    //metodo qu carga las opciones de movilidad y objetivos en los spinner
    private void cargarSpinners() {

        ArrayList<String> nivelesMovilidad = new ArrayList<>();
        nivelesMovilidad.add("Selecciona tu nivel");
        nivelesMovilidad.add("Baja movilidad");
        nivelesMovilidad.add("Movilidad moderada");
        nivelesMovilidad.add("Buena movilidad");
        nivelesMovilidad.add("Movilidad alta");

        ArrayAdapter<String> adapterMovilidad = new ArrayAdapter<>(this, R.layout.spinner_item, nivelesMovilidad);
        adapterMovilidad.setDropDownViewResource(R.layout.spinner_dropdowm_item);
        spNivelMovilidad.setAdapter(adapterMovilidad);

        ArrayList<String> objetivos = new ArrayList<>();
        objetivos.add("Selecciona tu objetivo");
        objetivos.add("Reducir dolor");
        objetivos.add("Mejorar movilidad");
        objetivos.add("Fortalecer músculos");
        objetivos.add("Recuperación física");
        objetivos.add("Mejorar equilibrio");

        ArrayAdapter<String> adapterObjetivos = new ArrayAdapter<>(this, R.layout.spinner_item, objetivos);
        adapterObjetivos.setDropDownViewResource(R.layout.spinner_dropdowm_item);
        spObjetivoPrincipal.setAdapter(adapterObjetivos);
    }

    //metood que asigna eventos a los radiobutton
    private void configurarRadioButtons() {
        rbNinguno.setOnClickListener(v -> seleccionarApoyo(rbNinguno));
        rbBaston.setOnClickListener(v -> seleccionarApoyo(rbBaston));
        rbMuletas.setOnClickListener(v -> seleccionarApoyo(rbMuletas));
        rbSillaRuedas.setOnClickListener(v -> seleccionarApoyo(rbSillaRuedas));
    }

    //permite que solo un apoyo fisico quede seleccionado
    private void seleccionarApoyo(RadioButton seleccionado) {
        rbNinguno.setChecked(false);
        rbBaston.setChecked(false);
        rbMuletas.setChecked(false);
        rbSillaRuedas.setChecked(false);
        seleccionado.setChecked(true);
        apoyoFisicoSeleccionado = seleccionado.getText().toString();   //guarda la opcion seleccionada
    }

    //configura el seekbar(barra) para seleccionar el nivel de dolor
    private void configurarSeekBar() {

        seekDolor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // actualiza el valor del dolor segun la posicion elegida
                nivelDolor = progress;

                // muestra el valor actual en pantalla
                txtValorDolor.setText(nivelDolor + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    //metodo qur valida los datos y guarda el perfil adaptado en firebase
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

            //evita errores si el usuario escribe texto en lugar de numeros
            edtEdad.setError("Ingresa una edad válida");
            return;
        }

        //valida que la edad este dentro de este rango
        if (edad <= 0 || edad > 100) {
            edtEdad.setError("Ingresa una edad válida");
            return;
        }

        //verificaciones de seleccion
        if (spNivelMovilidad.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tu nivel de movilidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (apoyoFisicoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona el apoyo físico que utilizas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spObjetivoPrincipal.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tu objetivo principal", Toast.LENGTH_SHORT).show();
            return;
        }

        String nivelMovilidad = spNivelMovilidad.getSelectedItem().toString();
        String objetivoPrincipal = spObjetivoPrincipal.getSelectedItem().toString();

        FirebaseUser usuarioActual = auth.getCurrentUser();

        //verifica que exista una sesion activa
        if (usuarioActual == null) {

            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PerfilAdaptadoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String uid = usuarioActual.getUid(); //obtiene el ID del usuario

        //se crea el objeto con toda la info
        PerfilAdaptado perfil = new PerfilAdaptado(edad, nivelMovilidad, apoyoFisicoSeleccionado, objetivoPrincipal, nivelDolor, true);

        //guarda el perfil en firebase
        usuariosRef.child(uid).child("perfilAdaptado").setValue(perfil).addOnSuccessListener(unused -> {

            Toast.makeText(this, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PerfilAdaptadoActivity.this, ZonaAfectadaActivity.class);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}