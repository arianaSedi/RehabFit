package com.example.rehabfit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.BienvenidaActivity;
import com.example.rehabfit.PerfilAdaptadoActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.ZonaAfectadaActivity;
import com.example.rehabfit.models.PerfilAdaptado;
import com.example.rehabfit.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PerfilFragment extends Fragment {

    private TextView txtAvatarPerfil;
    private TextView txtNombrePerfil;
    private TextView txtCorreoPerfil;
    private TextView txtSesionesPerfil;
    private TextView txtPublicacionesPerfil;

    private TextView txtEdadPerfil;
    private TextView txtMovilidadPerfil;
    private TextView txtApoyoPerfil;
    private TextView txtZonaPerfil;
    private TextView txtObjetivoPerfil;
    private TextView txtDolorPerfil;

    private AppCompatButton btnEditarPerfil;
    private AppCompatButton btnCambiarZona;
    private AppCompatButton btnMisConsultasIA;
    private AppCompatButton btnCerrarSesion;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    public PerfilFragment() {
        // Required empty public constructor
    }

    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_perfil, container, false);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        inicializarVistas(vista);
        cargarDatosPerfil();
        configurarBotones();
        return vista;
    }

    private void inicializarVistas(View vista) {
        txtAvatarPerfil = vista.findViewById(R.id.txtAvatarPerfil);
        txtNombrePerfil = vista.findViewById(R.id.txtNombrePerfil);
        txtCorreoPerfil = vista.findViewById(R.id.txtCorreoPerfil);
        txtSesionesPerfil = vista.findViewById(R.id.txtSesionesPerfil);
        txtPublicacionesPerfil = vista.findViewById(R.id.txtPublicacionesPerfil);
        txtEdadPerfil = vista.findViewById(R.id.txtEdadPerfil);
        txtMovilidadPerfil = vista.findViewById(R.id.txtMovilidadPerfil);
        txtApoyoPerfil = vista.findViewById(R.id.txtApoyoPerfil);
        txtZonaPerfil = vista.findViewById(R.id.txtZonaPerfil);
        txtObjetivoPerfil = vista.findViewById(R.id.txtObjetivoPerfil);
        btnEditarPerfil = vista.findViewById(R.id.btnEditarPerfil);
        btnCambiarZona = vista.findViewById(R.id.btnCambiarZona);
        btnMisConsultasIA = vista.findViewById(R.id.btnMisConsultasIA);
        btnCerrarSesion = vista.findViewById(R.id.btnCerrarSesion);
    }

    private void cargarDatosPerfil() {
        FirebaseUser usuarioActual = auth.getCurrentUser();

        if (usuarioActual == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "No hay usuario conectado", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String uid = usuarioActual.getUid();

        usuariosRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    if (!snapshot.exists()) {
                        txtNombrePerfil.setText("Usuario");
                        txtCorreoPerfil.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "Sin correo");
                        txtAvatarPerfil.setText("U");
                        mostrarPerfilVacio();
                        return;
                    }

                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario != null) {
                        String nombre = usuario.getNombre();
                        String correo = usuario.getCorreo();

                        if (nombre != null && !nombre.trim().isEmpty()) {
                            txtNombrePerfil.setText(nombre);
                            txtAvatarPerfil.setText(obtenerInicial(nombre));
                        } else {
                            txtNombrePerfil.setText("Usuario");
                            txtAvatarPerfil.setText("U");
                        }

                        if (correo != null && !correo.trim().isEmpty()) {
                            txtCorreoPerfil.setText(correo);
                        } else if (usuarioActual.getEmail() != null) {
                            txtCorreoPerfil.setText(usuarioActual.getEmail());
                        } else {
                            txtCorreoPerfil.setText("Sin correo");
                        }
                    }

                    PerfilAdaptado perfil = snapshot.child("perfilAdaptado").getValue(PerfilAdaptado.class);

                    if (perfil != null) {
                        txtEdadPerfil.setText(perfil.getEdad() + " años");
                        txtMovilidadPerfil.setText(perfil.getNivelMovilidad());
                        txtApoyoPerfil.setText(perfil.getApoyoFisico());
                        txtObjetivoPerfil.setText(perfil.getObjetivoPrincipal());

                        if (snapshot.child("perfilAdaptado").child("zonaAfectada").exists()) {
                            String zona = snapshot.child("perfilAdaptado").child("zonaAfectada").getValue(String.class);

                            if (zona != null && !zona.trim().isEmpty()) {
                                txtZonaPerfil.setText(zona);
                            } else {
                                txtZonaPerfil.setText("Sin seleccionar");
                            }
                        } else {
                            txtZonaPerfil.setText("Sin seleccionar");
                        }

                    } else {
                        mostrarPerfilVacio();
                    }

                    contarSesiones(uid);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
//metodo encargado de contar sesiones y publicaciones de cada usuario
    private void contarSesiones(String uid) {
        usuariosRef.child(uid).child("historialSesiones").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    txtSesionesPerfil.setText(String.valueOf(snapshot.getChildrenCount()));
                });

        usuariosRef.child(uid).child("publicaciones").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    txtPublicacionesPerfil.setText(String.valueOf(snapshot.getChildrenCount()));
                });
    }

    private String obtenerInicial(String nombre) {
        String nombreLimpio = nombre.trim();

        if (nombreLimpio.isEmpty()) {
            return "U";
        }

        return nombreLimpio.substring(0, 1).toUpperCase();
    }

    private void mostrarPerfilVacio() {
        txtEdadPerfil.setText("Sin datos");
        txtMovilidadPerfil.setText("Sin datos");
        txtApoyoPerfil.setText("Sin datos");
        txtZonaPerfil.setText("Sin seleccionar");
        txtObjetivoPerfil.setText("Sin datos");
    }

    private void configurarBotones() {
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PerfilAdaptadoActivity.class);
            startActivity(intent);
        });

        btnCambiarZona.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ZonaAfectadaActivity.class);
            startActivity(intent);
        });

        btnMisConsultasIA.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new ConsultasIAFragment())
                    .addToBackStack(null)
                    .commit();

        });

        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás segura de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    auth.signOut();

                    Intent intent = new Intent(requireContext(), BienvenidaActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}