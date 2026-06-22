package com.example.rehabfit.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.utils.RutinaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetalleEjerciciosFragment extends Fragment {

    private TextView btnVolverDetalle;
    private ImageView imgIconoDetalle;
    private TextView txtNombreDetalle;
    private TextView txtInfoDetalle;
    private TextView txtZonaDetalle;
    private TextView txtDificultadDetalle;
    private TextView txtPosicionDetalle;
    private TextView txtDuracionDetalle;
    private TextView txtRepeticionesDetalle;
    private TextView txtDescripcionDetalle;
    private LinearLayout contenedorInstruccionesDetalle;
    private TextView txtPrecaucionDetalle;
    private TextView btnAgregarRutinaDetalle;
    private TextView btnGuardarEjercicioDetalle;

    private Ejercicio ejercicioActual;
    private DatabaseReference favoritoRef;
    private boolean esFavorito = false;

    public DetalleEjerciciosFragment() {
    }

    public static DetalleEjerciciosFragment newInstance(Ejercicio ejercicio) {
        DetalleEjerciciosFragment fragment = new DetalleEjerciciosFragment();

        Bundle args = new Bundle();
        args.putSerializable("ejercicio", ejercicio);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_detalle_ejercicios, container, false);
        ocultarBottomNavigation();

        imgIconoDetalle = vista.findViewById(R.id.imgIconoDetalle);
        txtNombreDetalle = vista.findViewById(R.id.txtNombreDetalle);
        txtInfoDetalle = vista.findViewById(R.id.txtInfoDetalle);
        txtZonaDetalle = vista.findViewById(R.id.txtZonaDetalle);
        txtDificultadDetalle = vista.findViewById(R.id.txtDificultadDetalle);
        txtPosicionDetalle = vista.findViewById(R.id.txtPosicionDetalle);
        txtDuracionDetalle = vista.findViewById(R.id.txtDuracionDetalle);
        txtRepeticionesDetalle = vista.findViewById(R.id.txtRepeticionesDetalle);
        txtDescripcionDetalle = vista.findViewById(R.id.txtDescripcionDetalle);
        contenedorInstruccionesDetalle = vista.findViewById(R.id.contenedorInstruccionesDetalle);
        txtPrecaucionDetalle = vista.findViewById(R.id.txtPrecaucionDetalle);
        btnAgregarRutinaDetalle = vista.findViewById(R.id.btnAgregarRutinaDetalle);
        btnGuardarEjercicioDetalle = vista.findViewById(R.id.btnGuardarEjercicioDetalle);

        cargarDatosDelEjercicio();
        configurarBotones();
        return vista;
    }

    private void cargarDatosDelEjercicio() {
        if (getArguments() == null) {
            return;
        }

        ejercicioActual = (Ejercicio) getArguments().getSerializable("ejercicio");

        if (ejercicioActual == null) {
            return;
        }

        txtNombreDetalle.setText(valorSeguro(ejercicioActual.getNombre(), "Ejercicio"));

        txtInfoDetalle.setText("✓ " + valorSeguro(ejercicioActual.getNivel(), "Nivel no especificado") + " · Movilidad suave · Recomendado para " + valorSeguro(ejercicioActual.getZona(), "rehabilitación").toLowerCase());
        txtZonaDetalle.setText("Zona\n" + valorSeguro(ejercicioActual.getZona(), "No especificado"));
        txtDificultadDetalle.setText("Nivel\n" + valorSeguro(ejercicioActual.getNivel(), "No especificado"));
        txtPosicionDetalle.setText("Posición\n" + valorSeguro(ejercicioActual.getPosicion(), "No especificado"));
        txtDuracionDetalle.setText("Duración\n" + ejercicioActual.getDuracionMinutos() + " min");
        txtRepeticionesDetalle.setText("Repeticiones\n" + ejercicioActual.getRepeticiones() + " rep");
        txtDescripcionDetalle.setText(valorSeguro(ejercicioActual.getDescripcion(), "Sin descripción disponible"));

        cargarInstrucciones(crearInstrucciones(ejercicioActual));

        txtPrecaucionDetalle.setText("Precaución\n\n" + "• No realizar si causa dolor intenso al extender.\n" + "• Detente si aumenta la inflamación.\n" + "• Consulta con tu fisioterapeuta ante cualquier molestia.");

        imgIconoDetalle.setImageResource(obtenerIconoZona(ejercicioActual));

        prepararReferenciaFavorito();
        cargarEstadoFavorito();
    }

    private void configurarBotones() {

        btnAgregarRutinaDetalle.setOnClickListener(v -> {
            if (ejercicioActual == null) {
                return;
            }

            RutinaManager.agregarEjercicioConValidacion(ejercicioActual, new RutinaManager.AgregarCallback() {
                @Override
                public void onAgregado() {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Ejercicio agregado a tu rutina", Toast.LENGTH_SHORT).show();

                    irARutina();
                }

                @Override
                public void onYaExistia() {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Este ejercicio ya estaba en tu rutina", Toast.LENGTH_SHORT).show();
                    irARutina();
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Error al agregar a rutina: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        btnGuardarEjercicioDetalle.setOnClickListener(v -> cambiarFavorito());
    }

    private void irARutina() {
        mostrarBottomNavigation();

        requireActivity().getSupportFragmentManager().popBackStack();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_rutina);
        }
    }

    private void cargarInstrucciones(String[] pasos) {
        contenedorInstruccionesDetalle.removeAllViews();

        for (int i = 0; i < pasos.length; i++) {
            LinearLayout fila = new LinearLayout(requireContext());
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.TOP);
            fila.setPadding(0, 0, 0, convertirDp(10));

            TextView numero = new TextView(requireContext());
            numero.setWidth(convertirDp(18));
            numero.setHeight(convertirDp(18));
            numero.setGravity(Gravity.CENTER);
            numero.setText(String.valueOf(i + 1));
            numero.setTextColor(ContextCompat.getColor(requireContext(), R.color.blanco));
            numero.setTextSize(10);
            numero.setTypeface(null, Typeface.BOLD);
            numero.setBackgroundResource(R.drawable.bg_pasos);

            TextView texto = new TextView(requireContext());
            texto.setText(pasos[i]);
            texto.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            texto.setTextSize(13);
            texto.setLineSpacing(convertirDp(2), 1.0f);

            LinearLayout.LayoutParams paramsTexto = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            paramsTexto.setMargins(convertirDp(10), 0, 0, 0);
            texto.setLayoutParams(paramsTexto);

            fila.addView(numero);
            fila.addView(texto);

            contenedorInstruccionesDetalle.addView(fila);
        }
    }

    private int convertirDp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private void prepararReferenciaFavorito() {
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioActual == null || ejercicioActual == null) {
            favoritoRef = null;
            return;
        }

        String idEjercicio = obtenerIdEjercicio(ejercicioActual);

        favoritoRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuarioActual.getUid())
                .child("favoritos")
                .child(idEjercicio);
    }

    private void cargarEstadoFavorito() {
        if (favoritoRef == null) {
            pintarBotonFavorito(false);
            return;
        }

        favoritoRef.get()
                .addOnSuccessListener(snapshot -> {
                    esFavorito = snapshot.exists();
                    pintarBotonFavorito(esFavorito);
                })
                .addOnFailureListener(e -> {
                    esFavorito = false;
                    pintarBotonFavorito(false);
                });
    }

    private void cambiarFavorito() {
        if (favoritoRef == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (esFavorito) {
            favoritoRef.removeValue()
                    .addOnSuccessListener(unused -> {
                        esFavorito = false;
                        pintarBotonFavorito(false);

                        Toast.makeText(requireContext(), "Ejercicio quitado de favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al quitar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            favoritoRef.setValue(ejercicioActual)
                    .addOnSuccessListener(unused -> {
                        esFavorito = true;
                        pintarBotonFavorito(true);

                        Toast.makeText(requireContext(), "Ejercicio guardado en favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al guardar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void pintarBotonFavorito(boolean favorito) {
        if (favorito) {
            btnGuardarEjercicioDetalle.setText("★  Ejercicio guardado");
            btnGuardarEjercicioDetalle.setTextColor(ContextCompat.getColor(requireContext(), R.color.amarillo_estrella));
            btnGuardarEjercicioDetalle.setTypeface(null, Typeface.BOLD);
        } else {
            btnGuardarEjercicioDetalle.setText("☆  Guardar ejercicio");
            btnGuardarEjercicioDetalle.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_oscuro));
            btnGuardarEjercicioDetalle.setTypeface(null, Typeface.NORMAL);
        }
    }

    private String obtenerIdEjercicio(Ejercicio ejercicio) {
        if (ejercicio.getId() != 0) {
            return String.valueOf(ejercicio.getId());
        }

        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "sin_nombre";
        String zona = ejercicio.getZona() != null ? ejercicio.getZona() : "sin_zona";

        return limpiarTextoParaFirebase(nombre + "_" + zona);
    }

    private String limpiarTextoParaFirebase(String texto) {
        return texto.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_");
    }

    private String valorSeguro(String texto, String valorPorDefecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return valorPorDefecto;
        }
        return texto;
    }

    private String[] crearInstrucciones(Ejercicio ejercicio) {
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";
        String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";
        String posicion = ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";

        if (posicion.contains("sentado") && (zona.contains("rodilla") || nombre.contains("rodilla"))) {
            return new String[]{
                    "Siéntate en una silla con la espalda recta y pies apoyados.",
                    "Extiende lentamente una pierna hasta que quede recta.",
                    "Mantén la posición durante 5 segundos respirando tranquilo.",
                    "Baja la pierna lentamente sin dejar caer.",
                    "Repite 10 veces con cada pierna."
            };
        }

        if (posicion.contains("sentado")) {
            return new String[]{
                    "Siéntate en una silla estable.",
                    "Mantén la espalda recta y los pies apoyados.",
                    "Realiza el movimiento de forma lenta y controlada.",
                    "Evita forzar la zona trabajada.",
                    "Repite según las indicaciones del ejercicio."
            };
        }

        if (zona.contains("rodilla") || nombre.contains("rodilla")) {
            return new String[]{
                    "Colócate en una posición cómoda y estable.",
                    "Realiza el movimiento de rodilla lentamente.",
                    "Mantén el movimiento suave y controlado.",
                    "Regresa poco a poco a la posición inicial.",
                    "Detente si sientes dolor intenso."
            };
        }

        if (zona.contains("tobillo") || nombre.contains("tobillo")) {
            return new String[]{
                    "Apoya el pie de forma segura.",
                    "Realiza movimientos suaves con el tobillo.",
                    "Evita movimientos bruscos.",
                    "Regresa lentamente a la posición inicial.",
                    "Repite con cuidado."
            };
        }

        if (zona.contains("hombro") || nombre.contains("hombro")) {
            return new String[]{
                    "Mantén la espalda recta.",
                    "Relaja los hombros antes de iniciar.",
                    "Mueve el brazo lentamente.",
                    "No fuerces más allá de tu capacidad.",
                    "Regresa lentamente a la posición inicial."
            };
        }

        if (zona.contains("espalda") || nombre.contains("espalda")) {
            return new String[]{
                    "Mantén una postura cómoda y segura.",
                    "Realiza el movimiento lentamente.",
                    "Evita arquear demasiado la espalda.",
                    "Respira de forma controlada.",
                    "Detente si sientes dolor."
            };
        }

        return new String[]{
                "Colócate en una posición cómoda.",
                "Realiza el movimiento lentamente.",
                "No fuerces tu cuerpo.",
                "Descansa si sientes molestia.",
                "Repite según las indicaciones."
        };
    }

    private int obtenerIconoZona(Ejercicio ejercicio) {

        String zona = ejercicio.getZona().toLowerCase();

        if (zona.contains("rodilla")) {
            return R.drawable.rodilla;
        }

        if (zona.contains("pierna")) {
            return R.drawable.pierna;
        }

        if (zona.contains("hombro")) {
            return R.drawable.brazo;
        }

        if (zona.contains("tobillo")) {
            return R.drawable.tobillo;
        }

        if (zona.contains("espalda")) {
            return R.drawable.espalda;
        }

        if (zona.contains("mano")) {
            return R.drawable.mano;
        }

        if (zona.contains("brazo")) {
            return R.drawable.musculo;
        }

        if (zona.contains("muñeca") || zona.contains("muneca")) {
            return R.drawable.muneca;
        }

        return R.drawable.musculo;
    }

    private void ocultarBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mostrarBottomNavigation();
    }
}