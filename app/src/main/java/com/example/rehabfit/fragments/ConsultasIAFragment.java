package com.example.rehabfit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.rehabfit.R;
import com.example.rehabfit.models.ConsultasIA;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.models.IARequest;
import com.example.rehabfit.models.IAResponse;
import com.example.rehabfit.models.PerfilAdaptado;
import com.example.rehabfit.network.ApiService;
import com.example.rehabfit.network.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultasIAFragment extends Fragment {

    private EditText edtConsultaIA;
    private AppCompatButton btnGenerarIA;
    private AppCompatButton btnLimpiarIA;
    private TextView txtVerHistorialIA;
    private LinearLayout layoutResultadoIA;
    private TextView txtRespuestaIA;
    private TextView txtEjerciciosIA;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String movilidad = "No especificada";
    private String objetivo = "No especificado";
    private String apoyoFisico = "No especificado";
    private int dolorActual = 0;

    public ConsultasIAFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_consultas_ia, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        edtConsultaIA = vista.findViewById(R.id.edtConsultaIA);
        btnGenerarIA = vista.findViewById(R.id.btnGenerarIA);
        btnLimpiarIA = vista.findViewById(R.id.btnLimpiarIA);
        txtVerHistorialIA = vista.findViewById(R.id.txtVerHistorialIA);
        layoutResultadoIA = vista.findViewById(R.id.layoutResultadoIA);
        txtRespuestaIA = vista.findViewById(R.id.txtRespuestaIA);
        txtEjerciciosIA = vista.findViewById(R.id.txtEjerciciosIA);

        cargarPerfilUsuario();
        configurarEventos();

        return vista;
    }

    private void configurarEventos() {
        edtConsultaIA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean tieneTexto = s.toString().trim().length() >= 10;
                btnGenerarIA.setEnabled(tieneTexto);

                if (tieneTexto) {
                    btnGenerarIA.setBackgroundResource(R.drawable.bg_boton_verde);
                    btnGenerarIA.setTextColor(getResources().getColor(R.color.blanco));
                } else {
                    btnGenerarIA.setBackgroundResource(R.drawable.bg_chip_gris);
                    btnGenerarIA.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnGenerarIA.setOnClickListener(v -> generarRecomendacion());
        btnLimpiarIA.setOnClickListener(v -> edtConsultaIA.setText(""));
        txtVerHistorialIA.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new HistorialConsultaFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void cargarPerfilUsuario() {
        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .child("perfilAdaptado")
                .get()
                .addOnSuccessListener(snapshot -> {
                    PerfilAdaptado perfil = snapshot.getValue(PerfilAdaptado.class);

                    if (perfil != null) {
                        movilidad = valorSeguro(perfil.getNivelMovilidad(), "No especificada");
                        objetivo = valorSeguro(perfil.getObjetivoPrincipal(), "No especificado");
                        apoyoFisico = valorSeguro(perfil.getApoyoFisico(), "No especificado");
                        dolorActual = perfil.getNivelDolor();
                    }
                });
    }

    private void generarRecomendacion() {
        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String consulta = edtConsultaIA.getText().toString().trim();

        if (consulta.length() < 10) {
            Toast.makeText(requireContext(), "Escribe una consulta más detallada", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerarIA.setEnabled(false);
        btnGenerarIA.setText("Generando...");

        IARequest request = new IARequest(usuario.getUid(), consulta, movilidad, objetivo, apoyoFisico, dolorActual);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.generarRecomendacionIA(request).enqueue(new Callback<IAResponse>() {
            @Override
            public void onResponse(Call<IAResponse> call, Response<IAResponse> response) {
                if (!isAdded()) {
                    return;
                }

                String recomendacion;
                List<Ejercicio> ejerciciosRecomendados = new ArrayList<>();

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getRecomendacion() != null
                        && !response.body().getRecomendacion().trim().isEmpty()) {

                    recomendacion = response.body().getRecomendacion().trim();

                    if (response.body().getEjerciciosRecomendados() != null) {
                        ejerciciosRecomendados = response.body().getEjerciciosRecomendados();
                    }

                } else {
                    recomendacion = crearRecomendacionLocal(consulta);
                    ejerciciosRecomendados = Collections.emptyList();
                }

                guardarConsulta(usuario.getUid(), consulta, recomendacion, ejerciciosRecomendados);
            }

            @Override
            public void onFailure(Call<IAResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                String recomendacion = crearRecomendacionLocal(consulta);
                guardarConsulta(usuario.getUid(), consulta, recomendacion, Collections.emptyList());
            }
        });
    }

    private void guardarConsulta(String uid, String consulta, String recomendacion,
                                 List<Ejercicio> ejerciciosRecomendados) {

        String id = firestore.collection("users")
                .document(uid)
                .collection("consultasIA")
                .document()
                .getId();

        ConsultasIA consultaIA = new ConsultasIA(id, consulta, recomendacion, movilidad, objetivo, apoyoFisico, dolorActual, System.currentTimeMillis(), ejerciciosRecomendados);

        firestore.collection("users")
                .document(uid)
                .collection("consultasIA")
                .document(id)
                .set(consultaIA)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) {
                        return;
                    }

                    btnGenerarIA.setText("✈  Generar recomendación");
                    btnGenerarIA.setEnabled(true);

                    mostrarResultadoPantalla(recomendacion, ejerciciosRecomendados);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }

                    btnGenerarIA.setText("✈  Generar recomendación");
                    btnGenerarIA.setEnabled(true);

                    Toast.makeText(requireContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    mostrarResultadoPantalla(recomendacion, ejerciciosRecomendados);
                });
    }

    private void mostrarResultado(String recomendacionCompleta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Recomendación IA")
                .setMessage(recomendacionCompleta)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void mostrarDetalleHistorial(ConsultasIA consultaIA) {
        String detalle = "Consulta:\n" + consultaIA.getConsulta()
                + "\n\n"
                + construirTextoConEjercicios(
                consultaIA.getRecomendacion(),
                consultaIA.getEjerciciosRecomendados()
        );

        new AlertDialog.Builder(requireContext())
                .setTitle(formatearFecha(consultaIA.getFechaMillis()))
                .setMessage(detalle)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private String construirTextoConEjercicios(String recomendacion, List<Ejercicio> ejercicios) {
        StringBuilder builder = new StringBuilder();

        builder.append(recomendacion);

        if (ejercicios != null && !ejercicios.isEmpty()) {
            builder.append("\n\nEjercicios recomendados:\n\n");

            for (Ejercicio ejercicio : ejercicios) {
                builder.append("• ").append(valorSeguro(ejercicio.getNombre(), "Ejercicio")).append("\n");
                builder.append("  Zona: ").append(valorSeguro(ejercicio.getZona(), "No especificada")).append("\n");
                builder.append("  Nivel: ").append(valorSeguro(ejercicio.getNivel(), "No especificado")).append("\n");
                builder.append("  Posición: ").append(valorSeguro(ejercicio.getPosicion(), "No especificada")).append("\n");
                builder.append("  Duración: ").append(ejercicio.getDuracionMinutos()).append(" minutos\n");
                builder.append("  Repeticiones: ").append(ejercicio.getRepeticiones()).append("\n\n");
            }
        } else {
            builder.append("\n\nNo se encontraron ejercicios específicos desde la API.");
        }
        return builder.toString();
    }

    private String crearRecomendacionLocal(String consulta) {
        String consultaMinuscula = consulta.toLowerCase();
        String zona = "la zona afectada";

        if (consultaMinuscula.contains("rodilla")) {
            zona = "la rodilla";
        } else if (consultaMinuscula.contains("hombro")) {
            zona = "el hombro";
        } else if (consultaMinuscula.contains("espalda")) {
            zona = "la espalda";
        } else if (consultaMinuscula.contains("tobillo") || consultaMinuscula.contains("pie")) {
            zona = "el tobillo o pie";
        } else if (consultaMinuscula.contains("muñeca") || consultaMinuscula.contains("mano")) {
            zona = "la muñeca o mano";
        }

        return "Según tu consulta y tu perfil adaptado, puedes considerar ejercicios suaves para "
                + zona + ".\n\n"
                + "Cómo estructurar mejor tu consulta:\n"
                + "• Indica la zona afectada: rodilla, hombro, espalda, tobillo, mano, etc.\n"
                + "• Indica tu dolor actual del 0 al 10.\n"
                + "• Explica tu nivel de movilidad.\n"
                + "• Menciona tu objetivo: reducir dolor, mejorar movilidad o fortalecer.\n\n"
                + "Recomendación general:\n"
                + "• Realiza movilidad lenta y controlada durante 5 a 10 minutos.\n"
                + "• Mantén una intensidad baja, especialmente si tu dolor actual es "
                + dolorActual + "/10.\n"
                + "• Prioriza ejercicios en posición segura. Apoyo físico registrado: "
                + apoyoFisico + ".\n"
                + "• Evita movimientos bruscos, cargas pesadas o posiciones que aumenten el dolor.\n"
                + "• Detén la rutina si el dolor aumenta, aparece inflamación, mareo o sensación de inestabilidad.\n\n"
                + "Objetivo registrado: " + objetivo + ".\n\n"
                + "Esta recomendación es general y no sustituye la valoración de un médico o fisioterapeuta.";
    }

    private String valorSeguro(String texto, String defecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }

        return texto;
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) {
            return "Consulta";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo) + "...";
    }

    private String formatearFecha(long fechaMillis) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return formato.format(new Date(fechaMillis));
    }

    private void mostrarResultadoPantalla(String recomendacion, List<Ejercicio> ejercicios) {
        layoutResultadoIA.setVisibility(View.VISIBLE);

        txtRespuestaIA.setText(limpiarTextoIA(recomendacion));

        if (ejercicios != null && !ejercicios.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Ejercicios recomendados:\n\n");

            for (Ejercicio ejercicio : ejercicios) {
                builder.append("• ").append(valorSeguro(ejercicio.getNombre(), "Ejercicio")).append("\n");
                builder.append("  Zona: ").append(valorSeguro(ejercicio.getZona(), "No especificada"))
                        .append(" | Nivel: ").append(valorSeguro(ejercicio.getNivel(), "No especificado"))
                        .append("\n");

                builder.append("  Posición: ").append(valorSeguro(ejercicio.getPosicion(), "No especificada"))
                        .append(" | Duración: ").append(ejercicio.getDuracionMinutos())
                        .append(" min").append("\n\n");
            }

            txtEjerciciosIA.setText(builder.toString());
        } else {
            txtEjerciciosIA.setText("No se encontraron ejercicios específicos desde la API.");
        }
    }

    private String limpiarTextoIA(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace("**", "").replace("* ", "• ").replace("*", "•").trim();
    }
}