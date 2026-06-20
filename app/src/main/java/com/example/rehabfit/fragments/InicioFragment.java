package com.example.rehabfit.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.models.PerfilAdaptado;
import com.example.rehabfit.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.rehabfit.utils.RutinaManager;


public class InicioFragment extends Fragment {

    private TextView txtSaludo;
    private TextView txtSubSaludo;
    private TextView txtAvatar;

    private TextView txtSesionesSemana;
    private TextView txtTiempoTotal;
    private TextView txtZonaTrabajada;
    private TextView txtDolorPromedio;

    private AppCompatButton btnIniciarRutina;
    private AppCompatButton btnIrEjercicios;
    private AppCompatButton btnIrRutina;
    private AppCompatButton btnIrHistorial;
    private AppCompatButton btnIrProgreso;
    private AppCompatButton btnIrIA;
    private AppCompatButton btnIrComunidad;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    public InicioFragment() {
        // Required empty public constructor
    }

    public static InicioFragment newInstance(String param1, String param2) {
        InicioFragment fragment = new InicioFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_inicio, container, false);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        txtSaludo = vista.findViewById(R.id.txtSaludo);
        txtSubSaludo = vista.findViewById(R.id.txtSubSaludo);
        txtAvatar = vista.findViewById(R.id.txtAvatar);
        txtSesionesSemana = vista.findViewById(R.id.txtSesionesSemana);
        txtTiempoTotal = vista.findViewById(R.id.txtTiempoTotal);
        txtZonaTrabajada = vista.findViewById(R.id.txtZonaTrabajada);
        txtDolorPromedio = vista.findViewById(R.id.txtDolorPromedio);
        btnIniciarRutina = vista.findViewById(R.id.btnIniciarRutina);
        btnIrEjercicios = vista.findViewById(R.id.btnIrEjercicios);
        btnIrRutina = vista.findViewById(R.id.btnIrRutina);
        btnIrHistorial = vista.findViewById(R.id.btnIrHistorial);
        btnIrProgreso = vista.findViewById(R.id.btnIrProgreso);
        btnIrIA = vista.findViewById(R.id.btnIrIA);
        btnIrComunidad = vista.findViewById(R.id.btnIrComunidad);

        cargarDatosUsuario();
        configurarBotones();
        return vista;
    }

    private void cargarDatosUsuario() {
        FirebaseUser usuarioActual = auth.getCurrentUser();

        if (usuarioActual == null) {
            txtSaludo.setText("Hola");
            txtSubSaludo.setText("Inicia sesión para ver tu información");
            return;
        }

        String uid = usuarioActual.getUid();

        usuariosRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        txtSaludo.setText("Hola");
                        txtSubSaludo.setText("Bienvenida a RehabFit");
                        return;
                    }

                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario != null && usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
                        txtSaludo.setText("Hola, " + usuario.getNombre());

                        String inicial = usuario.getNombre().substring(0, 1).toUpperCase();
                        txtAvatar.setText(inicial);
                    } else {
                        txtSaludo.setText("Hola");
                    }

                    PerfilAdaptado perfil = snapshot.child("perfilAdaptado").getValue(PerfilAdaptado.class);

                    if (perfil != null) {
                        txtSubSaludo.setText("¿Lista para tu sesión de hoy?");
                        txtZonaTrabajada.setText(perfil.getObjetivoPrincipal() + "\nObjetivo");
                        txtDolorPromedio.setText(perfil.getNivelDolor() + "/10\nDolor actual");
                    } else {
                        txtSubSaludo.setText("Completa tu perfil adaptado");
                        txtZonaTrabajada.setText("Sin datos\nObjetivo");
                        txtDolorPromedio.setText("0/10\nDolor actual");
                    }

                    cargarResumenEstadisticas();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void configurarBotones() {
        txtAvatar.setOnClickListener(v -> irAPerfil());
        btnIniciarRutina.setOnClickListener(v -> irARutina());
        btnIrEjercicios.setOnClickListener(v -> irAEjercicios());
        btnIrRutina.setOnClickListener(v -> irARutina());
        btnIrComunidad.setOnClickListener(v -> irAComunidad());
        btnIrHistorial.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new HistorialFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnIrProgreso.setOnClickListener(v -> {getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new ProgresoFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnIrIA.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new ConsultasIAFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void irAPerfil() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_perfil);
        }
    }

    private void irAEjercicios() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_ejercicios);
        }
    }

    private void irARutina() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_rutina);
        }
    }

    private void irAComunidad() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_comunidad);
        }
    }
    private void cargarResumenEstadisticas() {
        RutinaManager.cargarResumenInicio((sesionesSemana, minutosTotales) -> {
            if (!isAdded()) {
                return;
            }

            txtSesionesSemana.setText(sesionesSemana + "\nSesiones esta semana");
            txtTiempoTotal.setText(minutosTotales + " min\nTiempo total");
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        if (txtSesionesSemana != null && txtTiempoTotal != null) {
            cargarResumenEstadisticas();
        }
    }
}