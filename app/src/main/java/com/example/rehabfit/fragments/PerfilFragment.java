package com.example.rehabfit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
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
import com.example.rehabfit.fragments.FavoritosFragment;
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
    private AppCompatButton btnMisFavoritos;

    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    public PerfilFragment() {
        // Required empty public constructor
    }

    // metodo para crear una nueva instancia del fragment
    public static PerfilFragment newInstance(String param1, String param2) {

        // crea una nueva instancia de perfil fragment
        PerfilFragment fragment = new PerfilFragment();

        // crea un bundle para enviar datos al fragment
        Bundle args = new Bundle();

        // devuelve el fragment creado
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // infla el layout del fragment de perfil
        View vista = inflater.inflate(R.layout.fragment_perfil, container, false);

        // inicializa firebase auth
        auth = FirebaseAuth.getInstance();

        // obtiene la referencia al nodo usuarios en firebase
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // vincula los componentes visuales
        inicializarVistas(vista);

        // carga los datos del perfil del usuario
        cargarDatosPerfil();

        // configura los eventos de los botones
        configurarBotones();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de vincular las variables con los elementos del layout
    private void inicializarVistas(View vista) {

        // vincula los textos del perfil
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

        // vincula los botones del perfil
        btnEditarPerfil = vista.findViewById(R.id.btnEditarPerfil);
        btnCambiarZona = vista.findViewById(R.id.btnCambiarZona);
        btnMisConsultasIA = vista.findViewById(R.id.btnMisConsultasIA);
        btnCerrarSesion = vista.findViewById(R.id.btnCerrarSesion);
        btnMisFavoritos = vista.findViewById(R.id.btnMisFavoritos);
    }

    // metodo encargado de cargar los datos del perfil desde firebase
    private void cargarDatosPerfil() {

        // obtiene el usuario autenticado
        FirebaseUser usuarioActual = auth.getCurrentUser();

        // valida si no hay usuario conectado
        if (usuarioActual == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "No hay usuario conectado", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // obtiene el uid del usuario actual
        String uid = usuarioActual.getUid();

        // busca los datos del usuario en firebase
        usuariosRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // si no existen datos del usuario, muestra datos por defecto
                    if (!snapshot.exists()) {
                        txtNombrePerfil.setText("Usuario");
                        txtCorreoPerfil.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "Sin correo");
                        txtAvatarPerfil.setText("U");
                        mostrarPerfilVacio();
                        return;
                    }

                    // convierte los datos obtenidos en un objeto usuario
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario != null) {

                        // obtiene nombre y correo del usuario
                        String nombre = usuario.getNombre();
                        String correo = usuario.getCorreo();

                        // valida si existe nombre
                        if (nombre != null && !nombre.trim().isEmpty()) {
                            txtNombrePerfil.setText(nombre);
                            txtAvatarPerfil.setText(obtenerInicial(nombre));
                        } else {
                            txtNombrePerfil.setText("Usuario");
                            txtAvatarPerfil.setText("U");
                        }

                        // valida si existe correo en la base de datos
                        if (correo != null && !correo.trim().isEmpty()) {
                            txtCorreoPerfil.setText(correo);
                        } else if (usuarioActual.getEmail() != null) {

                            // si no existe correo en la base, usa el correo de auth
                            txtCorreoPerfil.setText(usuarioActual.getEmail());
                        } else {

                            // si no hay correo, muestra texto por defecto
                            txtCorreoPerfil.setText("Sin correo");
                        }
                    }

                    // obtiene el perfil adaptado del usuario
                    PerfilAdaptado perfil = snapshot.child("perfilAdaptado").getValue(PerfilAdaptado.class);

                    // valida si existe perfil adaptado
                    if (perfil != null) {

                        // muestra los datos del perfil adaptado
                        txtEdadPerfil.setText(perfil.getEdad() + " años");
                        txtMovilidadPerfil.setText(perfil.getNivelMovilidad());
                        txtApoyoPerfil.setText(perfil.getApoyoFisico());
                        txtObjetivoPerfil.setText(perfil.getObjetivoPrincipal());

                        // valida si existe la zona afectada
                        if (snapshot.child("perfilAdaptado").child("zonaAfectada").exists()) {
                            String zona = snapshot.child("perfilAdaptado").child("zonaAfectada").getValue(String.class);

                            // muestra la zona si tiene valor
                            if (zona != null && !zona.trim().isEmpty()) {
                                txtZonaPerfil.setText(zona);
                            } else {
                                txtZonaPerfil.setText("Sin seleccionar");
                            }
                        } else {
                            txtZonaPerfil.setText("Sin seleccionar");
                        }

                    } else {

                        // muestra datos vacios si no existe perfil adaptado
                        mostrarPerfilVacio();
                    }

                    // cuenta las sesiones y publicaciones del usuario
                    contarSesiones(uid);
                })
                .addOnFailureListener(e -> {

                    // valida que el fragment siga activo
                    if (!isAdded()) {
                        return;
                    }

                    // muestra mensaje si ocurre un error al cargar perfil
                    Toast.makeText(requireContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // metodo encargado de contar sesiones y publicaciones de cada usuario
    private void contarSesiones(String uid) {

        // obtiene la cantidad de sesiones guardadas del usuario
        usuariosRef.child(uid).child("sesiones").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;

                    // muestra el total de sesiones en pantalla
                    txtSesionesPerfil.setText(String.valueOf(snapshot.getChildrenCount()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) txtSesionesPerfil.setText("0");
                });

        // obtiene la cantidad de publicaciones guardadas del usuario
        usuariosRef.child(uid).child("publicaciones").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;

                    // muestra el total de publicaciones en pantalla
                    txtPublicacionesPerfil.setText(String.valueOf(snapshot.getChildrenCount()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) txtPublicacionesPerfil.setText("0");
                });
    }

    // metodo para obtener la primera letra del nombre
    private String obtenerInicial(String nombre) {

        // elimina espacios al inicio y al final
        String nombreLimpio = nombre.trim();

        // si el nombre esta vacio, devuelve u
        if (nombreLimpio.isEmpty()) {
            return "U";
        }

        // devuelve la primera letra en mayuscula
        return nombreLimpio.substring(0, 1).toUpperCase();
    }

    // metodo para mostrar valores por defecto cuando no hay perfil adaptado
    private void mostrarPerfilVacio() {

        // muestra textos por defecto
        txtEdadPerfil.setText("Sin datos");
        txtMovilidadPerfil.setText("Sin datos");
        txtApoyoPerfil.setText("Sin datos");
        txtZonaPerfil.setText("Sin seleccionar");
        txtObjetivoPerfil.setText("Sin datos");
    }

    // metodo encargado de configurar los botones del perfil
    private void configurarBotones() {
        // abre la pantalla para editar perfil adaptado
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PerfilAdaptadoActivity.class);
            startActivity(intent);
        });

        // abre la pantalla para cambiar la zona afectada
        btnCambiarZona.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ZonaAfectadaActivity.class);
            startActivity(intent);
        });

        // abre el fragment de consultas ia
        btnMisConsultasIA.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new ConsultasIAFragment())
                    .addToBackStack(null)
                    .commit();

        });

        // abre el fragment de favoritos
        btnMisFavoritos.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new FavoritosFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // muestra confirmacion para cerrar sesion
        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }

    // metodo encargado de mostrar el dialogo de cierre de sesion
    private void mostrarDialogoCerrarSesion() {

        // crea un dialogo para confirmar el cierre de sesion
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Si, cerrar sesión", (d, which) -> {
                    // cierra la sesion en firebase
                    auth.signOut();
                    // crea el intent para volver a la pantalla de bienvenida
                    Intent intent = new Intent(requireContext(), BienvenidaActivity.class);
                    // limpia las actividades anteriores
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // abre la pantalla de bienvenida
                    startActivity(intent);

                })
                .setNegativeButton("Cancelar", null)
                .create();

        // cambia el color de los botones del dialogo
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_principal));
        });

        // muestra el dialogo
        dialog.show();
    }
}