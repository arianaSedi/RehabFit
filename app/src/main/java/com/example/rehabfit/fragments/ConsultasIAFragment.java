package com.example.rehabfit.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// fragment encargado de gestionar las consultas realizadas a la inteligencia artificial
public class ConsultasIAFragment extends Fragment {

    private EditText edtConsultaIA;
    private AppCompatButton btnGenerarIA;
    private ImageButton btnLimpiarIA;
    private TextView txtVerHistorialIA;
    private LinearLayout layoutResultadoIA;
    private TextView txtRespuestaIA;
    private TextView txtEjerciciosIA;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    private String movilidad = "No especificada";
    private String objetivo = "No especificado";
    private String apoyoFisico = "No especificado";
    private int dolorActual = 0;

    // constructor vacio requerido por fragment
    public ConsultasIAFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout del fragment
        View vista = inflater.inflate(R.layout.fragment_consultas_ia, container, false);

        // inicializa firebase auth
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // vincula los componentes visuales
        edtConsultaIA = vista.findViewById(R.id.edtConsultaIA);
        btnGenerarIA = vista.findViewById(R.id.btnGenerarIA);
        btnLimpiarIA = vista.findViewById(R.id.btnLimpiarIA);
        txtVerHistorialIA = vista.findViewById(R.id.txtVerHistorialIA);
        layoutResultadoIA = vista.findViewById(R.id.layoutResultadoIA);
        txtRespuestaIA = vista.findViewById(R.id.txtRespuestaIA);
        txtEjerciciosIA = vista.findViewById(R.id.txtEjerciciosIA);

        // carga el perfil adaptado del usuario
        cargarPerfilUsuario();
        // configura eventos de botones y campos
        configurarEventos();

        return vista;
    }

    // metodo encargado de configurar eventos de la interfaz
    private void configurarEventos() {
        // detecta cambios en el campo de texto
        edtConsultaIA.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // verifica si la consulta tiene al menos 10 caracteres
                boolean tieneTexto = s.toString().trim().length() >= 10;
                // habilita o deshabilita el boton
                btnGenerarIA.setEnabled(tieneTexto);

                if (tieneTexto) {
                    // aplica estilo activo al boton
                    btnGenerarIA.setBackgroundResource(R.drawable.bg_boton_verde);
                    btnGenerarIA.setTextColor(getResources().getColor(R.color.blanco));

                } else {
                    // aplica estilo deshabilitado al boton
                    btnGenerarIA.setBackgroundResource(R.drawable.bg_chip_gris);
                    btnGenerarIA.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // genera una recomendacion al presionar el boton
        btnGenerarIA.setOnClickListener(v -> generarRecomendacion());
        // limpia el contenido de la consulta
        btnLimpiarIA.setOnClickListener(v -> edtConsultaIA.setText(""));
        // abre el historial de consultas realizadas
        txtVerHistorialIA.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new HistorialConsultaFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // carga el perfil adaptado almacenado del usuario
    private void cargarPerfilUsuario() {

        FirebaseUser usuario = auth.getCurrentUser();

        // valida que exista una sesion iniciada
        if (usuario == null) {
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .child("perfilAdaptado")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // convierte los datos obtenidos en un objeto perfil adaptado
                    PerfilAdaptado perfil = snapshot.getValue(PerfilAdaptado.class);

                    if (perfil != null) {
                        // guarda los datos del perfil para enviarlos a la ia
                        movilidad = valorSeguro(perfil.getNivelMovilidad(), "No especificada");
                        objetivo = valorSeguro(perfil.getObjetivoPrincipal(), "No especificado");
                        apoyoFisico = valorSeguro(perfil.getApoyoFisico(), "No especificado");
                        dolorActual = perfil.getNivelDolor();
                    }
                });
    }

    // genera una recomendacion utilizando la api de inteligencia artificial
    private void generarRecomendacion() {
        // obtiene el usuario autenticado
        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // obtiene la consulta escrita por el usuario
        String consulta = edtConsultaIA.getText().toString().trim();

        // valida longitud minima de la consulta
        if (consulta.length() < 10) {
            Toast.makeText(requireContext(), "Escribe una consulta más detallada", Toast.LENGTH_SHORT).show();
            return;
        }

        // bloquea el boton mientras se procesa la solicitud
        btnGenerarIA.setEnabled(false);
        btnGenerarIA.setText("Generando...");

        // crea el objeto request con los datos del usuario
        IARequest request = new IARequest(
                usuario.getUid(),
                consulta,
                movilidad,
                objetivo,
                apoyoFisico,
                dolorActual
        );

        // obtiene la interfaz de la api
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // envia la solicitud a la api
        apiService.generarRecomendacionIA(request).enqueue(new Callback<IAResponse>() {

            @Override
            public void onResponse(Call<IAResponse> call, Response<IAResponse> response) {
                // valida que el fragment siga activo
                if (!isAdded()) {
                    return;
                }

                String recomendacion;
                List<Ejercicio> ejerciciosRecomendados = new ArrayList<>();

                // verifica que la respuesta de la api sea valida
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getRecomendacion() != null
                        && !response.body().getRecomendacion().trim().isEmpty()) {

                    // obtiene la recomendacion enviada por la api
                    recomendacion = response.body().getRecomendacion().trim();

                    // obtiene la lista de ejercicios recomendados
                    if (response.body().getEjerciciosRecomendados() != null) {
                        ejerciciosRecomendados = response.body().getEjerciciosRecomendados();
                    }

                } else {
                    // genera una recomendacion local si la api falla
                    recomendacion = crearRecomendacionLocal(consulta);
                    ejerciciosRecomendados = Collections.emptyList();
                }

                // guarda la consulta realizada
                guardarConsulta(usuario.getUid(), consulta, recomendacion, ejerciciosRecomendados);
            }

            @Override
            public void onFailure(Call<IAResponse> call, Throwable t) {
                // valida que el fragment siga activo
                if (!isAdded()) {
                    return;
                }

                // crea una recomendacion local cuando falla la conexion
                String recomendacion = crearRecomendacionLocal(consulta);
                // guarda la consulta realizada
                guardarConsulta(usuario.getUid(), consulta, recomendacion, Collections.emptyList());
            }
        });
    }

    // metodo para guardar la consulta realizada por el usuario en realtime database
    private void guardarConsulta(String uid, String consulta, String recomendacion, List<Ejercicio> ejerciciosRecomendados) {

        // crea la referencia al nodo donde se guardaran las consultas ia del usuario
        DatabaseReference consultasRef = usuariosRef
                .child(uid)
                .child("consultasIA");

        // genera un id unico para la nueva consulta
        String id = consultasRef.push().getKey();

        // valida que el id se haya generado correctamente
        if (id == null) {
            Toast.makeText(requireContext(), "Error al generar ID de consulta", Toast.LENGTH_LONG).show();
            return;
        }

        // crea el objeto consulta ia con todos los datos necesarios
        ConsultasIA consultaIA = new ConsultasIA(
                id,
                consulta,
                recomendacion,
                movilidad,
                objetivo,
                apoyoFisico,
                dolorActual,
                System.currentTimeMillis(),
                ejerciciosRecomendados
        );

        // guarda la consulta dentro del nodo consultas ia usando el id generado
        consultasRef.child(id)
                .setValue(consultaIA)
                .addOnSuccessListener(unused -> {

                    // valida que el fragment siga activo antes de modificar la interfaz
                    if (!isAdded()) return;

                    // restaura el texto original del boton
                    btnGenerarIA.setText("✈  Generar recomendación");

                    // habilita nuevamente el boton
                    btnGenerarIA.setEnabled(true);

                    // muestra la recomendacion y los ejercicios en pantalla
                    mostrarResultadoPantalla(recomendacion, ejerciciosRecomendados);
                })
                .addOnFailureListener(e -> {

                    // valida que el fragment siga activo antes de modificar la interfaz
                    if (!isAdded()) return;

                    // restaura el texto original del boton
                    btnGenerarIA.setText("✈  Generar recomendación");

                    // habilita nuevamente el boton
                    btnGenerarIA.setEnabled(true);

                    // muestra mensaje si ocurre un error al guardar
                    Toast.makeText(requireContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // aunque falle el guardado, muestra la recomendacion en pantalla
                    mostrarResultadoPantalla(recomendacion, ejerciciosRecomendados);
                });
    }

    // metodo para unir la recomendacion con los ejercicios recomendados en un solo texto
    private String construirTextoConEjercicios(String recomendacion, List<Ejercicio> ejercicios) {
        // permite construir textos largos de forma ordenada
        StringBuilder builder = new StringBuilder();

        // agrega primero la recomendacion
        builder.append(recomendacion);

        // valida si existen ejercicios recomendados
        if (ejercicios != null && !ejercicios.isEmpty()) {
            // agrega el titulo de la seccion de ejercicios
            builder.append("\n\nEjercicios recomendados:\n\n");

            // recorre cada ejercicio recomendado
            for (Ejercicio ejercicio : ejercicios) {
                // agrega el nombre del ejercicio
                builder.append("• ").append(valorSeguro(ejercicio.getNombre(), "Ejercicio")).append("\n");
                // agrega la zona del ejercicio
                builder.append("  Zona: ").append(valorSeguro(ejercicio.getZona(), "No especificada")).append("\n");

                // agrega el nivel del ejercicio
                builder.append("  Nivel: ").append(valorSeguro(ejercicio.getNivel(), "No especificado")).append("\n");

                // agrega la posicion del ejercicio
                builder.append("  Posición: ").append(valorSeguro(ejercicio.getPosicion(), "No especificada")).append("\n");

                // agrega la duracion del ejercicio
                builder.append("  Duración: ").append(ejercicio.getDuracionMinutos()).append(" minutos\n");

                // agrega la cantidad de repeticiones
                builder.append("  Repeticiones: ").append(ejercicio.getRepeticiones()).append("\n\n");
            }
        } else {

            // mensaje cuando no se reciben ejercicios desde la api
            builder.append("\n\nNo se encontraron ejercicios específicos desde la API.");
        }

        // devuelve el texto completo construido
        return builder.toString();
    }

    // metodo para crear una recomendacion local cuando la api no responde
    private String crearRecomendacionLocal(String consulta) {

        // convierte la consulta a minusculas para facilitar la busqueda de palabras clave
        String consultaMinuscula = consulta.toLowerCase();

        // zona por defecto si no se identifica una parte especifica del cuerpo
        String zona = "la zona afectada";

        // identifica si la consulta menciona rodilla
        if (consultaMinuscula.contains("rodilla")) {
            zona = "la rodilla";

            // identifica si la consulta menciona hombro
        } else if (consultaMinuscula.contains("hombro")) {
            zona = "el hombro";

            // identifica si la consulta menciona espalda
        } else if (consultaMinuscula.contains("espalda")) {
            zona = "la espalda";

            // identifica si la consulta menciona tobillo o pie
        } else if (consultaMinuscula.contains("tobillo") || consultaMinuscula.contains("pie")) {
            zona = "el tobillo o pie";

            // identifica si la consulta menciona muneca o mano
        } else if (consultaMinuscula.contains("muñeca") || consultaMinuscula.contains("mano")) {
            zona = "la muñeca o mano";
        }

        // devuelve una recomendacion general usando los datos del perfil adaptado
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

    // metodo para evitar valores nulos o vacios
    private String valorSeguro(String texto, String defecto) {

        // si el texto no existe o esta vacio, devuelve el valor por defecto
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }

        // devuelve el texto original si es valido
        return texto;
    }

    // metodo para mostrar la recomendacion y ejercicios directamente en la pantalla
    private void mostrarResultadoPantalla(String recomendacion, List<Ejercicio> ejercicios) {
        // hace visible el contenedor del resultado
        layoutResultadoIA.setVisibility(View.VISIBLE);

        // limpia el formato del texto y lo muestra en pantalla
        txtRespuestaIA.setText(limpiarTextoIA(recomendacion));

        // valida si existen ejercicios recomendados
        if (ejercicios != null && !ejercicios.isEmpty()) {
            // construye el texto de ejercicios recomendados
            StringBuilder builder = new StringBuilder();
            builder.append("Ejercicios recomendados:\n\n");

            // recorre cada ejercicio recomendado
            for (Ejercicio ejercicio : ejercicios) {
                // agrega el nombre del ejercicio
                builder.append("• ").append(valorSeguro(ejercicio.getNombre(), "Ejercicio")).append("\n");
                // agrega zona y nivel del ejercicio
                builder.append("  Zona: ").append(valorSeguro(ejercicio.getZona(), "No especificada"))
                        .append(" | Nivel: ").append(valorSeguro(ejercicio.getNivel(), "No especificado"))
                        .append("\n");

                // agrega posicion y duracion del ejercicio
                builder.append("  Posición: ").append(valorSeguro(ejercicio.getPosicion(), "No especificada"))
                        .append(" | Duración: ").append(ejercicio.getDuracionMinutos())
                        .append(" min").append("\n\n");
            }

            // muestra los ejercicios en pantalla
            txtEjerciciosIA.setText(builder.toString());

        } else {
            // muestra mensaje si no hay ejercicios recomendados
            txtEjerciciosIA.setText("No se encontraron ejercicios específicos desde la API.");
        }
    }

    // metodo para limpiar simbolos de formato enviados por la ia
    private String limpiarTextoIA(String texto) {
        // si el texto es nulo, devuelve texto vacio
        if (texto == null) {
            return "";
        }

        // elimina asteriscos y cambia viñetas por un formato mas claro
        return texto.replace("**", "").replace("* ", "• ").replace("*", "•").trim();
    }
}