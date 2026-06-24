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

import com.bumptech.glide.Glide;
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

    // metodo para crear una nueva instancia del fragment enviando un ejercicio seleccionado
    public static DetalleEjerciciosFragment newInstance(Ejercicio ejercicio) {

        // crea una nueva instancia del fragment
        DetalleEjerciciosFragment fragment = new DetalleEjerciciosFragment();

        // crea un bundle para enviar el ejercicio al fragment
        Bundle args = new Bundle();

        // guarda el ejercicio dentro del bundle
        args.putSerializable("ejercicio", ejercicio);

        // asigna los argumentos al fragment
        fragment.setArguments(args);

        // devuelve el fragment configurado
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // infla el layout del detalle de ejercicios
        View vista = inflater.inflate(R.layout.fragment_detalle_ejercicios, container, false);

        // oculta el menu inferior mientras se muestra el detalle
        ocultarBottomNavigation();

        // vincula todos los componentes visuales del layout
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

        // carga los datos del ejercicio seleccionado
        cargarDatosDelEjercicio();

        // configura los eventos de los botones
        configurarBotones();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar la informacion del ejercicio
    private void cargarDatosDelEjercicio() {

        // valida que existan argumentos enviados al fragment
        if (getArguments() == null) {
            return;
        }

        // recupera el ejercicio enviado desde el fragment anterior
        ejercicioActual = (Ejercicio) getArguments().getSerializable("ejercicio");

        // valida que el ejercicio exista
        if (ejercicioActual == null) {
            return;
        }

        // muestra el nombre del ejercicio
        txtNombreDetalle.setText(valorSeguro(ejercicioActual.getNombre(), "Ejercicio"));

        // muestra informacion general del ejercicio
        txtInfoDetalle.setText("✓ " + valorSeguro(ejercicioActual.getNivel(), "Nivel no especificado") + " · Movilidad suave · Recomendado para " + valorSeguro(ejercicioActual.getZona(), "rehabilitación").toLowerCase());
        // muestra la zona trabajada
        txtZonaDetalle.setText("Zona\n" + valorSeguro(ejercicioActual.getZona(), "No especificado"));
        // muestra el nivel de dificultad
        txtDificultadDetalle.setText("Nivel\n" + valorSeguro(ejercicioActual.getNivel(), "No especificado"));
        // muestra la posicion recomendada
        txtPosicionDetalle.setText("Posición\n" + valorSeguro(ejercicioActual.getPosicion(), "No especificado"));
        // muestra la duracion del ejercicio
        txtDuracionDetalle.setText("Duración\n" + ejercicioActual.getDuracionMinutos() + " min");
        // muestra la cantidad de repeticiones
        txtRepeticionesDetalle.setText("Repeticiones\n" + ejercicioActual.getRepeticiones() + " rep");
        // muestra la descripcion del ejercicio
        txtDescripcionDetalle.setText(valorSeguro(ejercicioActual.getDescripcion(), "Sin descripción disponible"));
        // genera y carga las instrucciones correspondientes
        cargarInstrucciones(crearInstrucciones(ejercicioActual));
        // muestra recomendaciones de seguridad
        txtPrecaucionDetalle.setText("Precaución\n\n"
                + "• No realizar si causa dolor intenso al extender.\n"
                + "• Detente si aumenta la inflamación.\n"
                + "• Consulta con tu fisioterapeuta ante cualquier molestia.");

        // carga la imagen desde la api usando glide
        if (ejercicioActual.getImagen() != null && !ejercicioActual.getImagen().isEmpty()) {

            Glide.with(requireContext())
                    .load(ejercicioActual.getImagen())
                    .placeholder(obtenerIconoZona(ejercicioActual))
                    .error(obtenerIconoZona(ejercicioActual))
                    .fitCenter()
                    .into(imgIconoDetalle);

        } else {
            // si no existe imagen, muestra un icono local
            imgIconoDetalle.setImageResource(obtenerIconoZona(ejercicioActual));
        }

        // prepara la referencia del favorito en firebase
        prepararReferenciaFavorito();
        // carga el estado actual del favorito
        cargarEstadoFavorito();
    }

    // metodo encargado de configurar los botones de la pantalla
    private void configurarBotones() {

        // boton para agregar el ejercicio a la rutina
        btnAgregarRutinaDetalle.setOnClickListener(v -> {
            // valida que exista un ejercicio seleccionado
            if (ejercicioActual == null) {
                return;
            }

            // agrega el ejercicio a la rutina validando duplicados
            RutinaManager.agregarEjercicioConValidacion(ejercicioActual, new RutinaManager.AgregarCallback() {

                @Override
                public void onAgregado() {

                    if (!isAdded()) {
                        return;
                    }

                    // mensaje cuando el ejercicio fue agregado correctamente
                    Toast.makeText(requireContext(), "Ejercicio agregado a tu rutina", Toast.LENGTH_SHORT).show();

                    // redirige a la pantalla de rutina
                    irARutina();
                }

                @Override
                public void onYaExistia() {

                    if (!isAdded()) {
                        return;
                    }

                    // mensaje cuando el ejercicio ya estaba agregado
                    Toast.makeText(requireContext(), "Este ejercicio ya estaba en tu rutina", Toast.LENGTH_SHORT).show();

                    // redirige a la rutina
                    irARutina();
                }

                @Override
                public void onError(String error) {

                    if (!isAdded()) {
                        return;
                    }

                    // muestra mensaje de error
                    Toast.makeText(requireContext(), "Error al agregar a rutina: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        // boton para guardar o quitar favoritos
        btnGuardarEjercicioDetalle.setOnClickListener(v -> cambiarFavorito());
    }

    // metodo para regresar a la pantalla de rutina
    private void irARutina() {
        // vuelve a mostrar el menu inferior
        mostrarBottomNavigation();

        // regresa al fragment anterior
        requireActivity().getSupportFragmentManager().popBackStack();

        // cambia directamente al fragment rutina
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_rutina);
        }
    }
    // metodo para cargar las instrucciones del ejercicio en pantalla
    private void cargarInstrucciones(String[] pasos) {

        // limpia el contenedor antes de agregar nuevas instrucciones
        contenedorInstruccionesDetalle.removeAllViews();

        // recorre cada paso recibido
        for (int i = 0; i < pasos.length; i++) {

            // crea una fila horizontal para mostrar el numero y el texto del paso
            LinearLayout fila = new LinearLayout(requireContext());
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.TOP);
            fila.setPadding(0, 0, 0, convertirDp(10));

            // crea el texto que muestra el numero del paso
            TextView numero = new TextView(requireContext());
            numero.setWidth(convertirDp(18));
            numero.setHeight(convertirDp(18));
            numero.setGravity(Gravity.CENTER);
            numero.setText(String.valueOf(i + 1));
            numero.setTextColor(ContextCompat.getColor(requireContext(), R.color.blanco));
            numero.setTextSize(10);
            numero.setTypeface(null, Typeface.BOLD);
            numero.setBackgroundResource(R.drawable.bg_pasos);

            // crea el texto donde se muestra la instruccion
            TextView texto = new TextView(requireContext());
            texto.setText(pasos[i]);
            texto.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            texto.setTextSize(13);
            texto.setLineSpacing(convertirDp(2), 1.0f);

            // define el espacio que ocupara el texto dentro de la fila
            LinearLayout.LayoutParams paramsTexto = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            paramsTexto.setMargins(convertirDp(10), 0, 0, 0);
            texto.setLayoutParams(paramsTexto);

            // agrega el numero y el texto a la fila
            fila.addView(numero);
            fila.addView(texto);

            // agrega la fila al contenedor principal
            contenedorInstruccionesDetalle.addView(fila);
        }
    }

    // metodo para convertir valores dp a pixeles
    private int convertirDp(int valor) {

        // multiplica el valor por la densidad de pantalla del dispositivo
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    // metodo para preparar la referencia del ejercicio favorito en firebase
    private void prepararReferenciaFavorito() {

        // obtiene el usuario autenticado actualmente
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        // valida que exista usuario y ejercicio actual
        if (usuarioActual == null || ejercicioActual == null) {
            favoritoRef = null;
            return;
        }

        // obtiene el id del ejercicio actual
        String idEjercicio = obtenerIdEjercicio(ejercicioActual);

        // crea la referencia donde se guardara el favorito del usuario
        favoritoRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuarioActual.getUid())
                .child("favoritos")
                .child(idEjercicio);
    }

    // metodo para cargar si el ejercicio ya esta guardado como favorito
    private void cargarEstadoFavorito() {

        // si no existe referencia, se muestra el boton como no favorito
        if (favoritoRef == null) {
            pintarBotonFavorito(false);
            return;
        }

        // consulta en firebase si el favorito existe
        favoritoRef.get()
                .addOnSuccessListener(snapshot -> {

                    // guarda el estado del favorito segun si existe en firebase
                    esFavorito = snapshot.exists();

                    // actualiza visualmente el boton
                    pintarBotonFavorito(esFavorito);
                })
                .addOnFailureListener(e -> {

                    // si ocurre error, se marca como no favorito
                    esFavorito = false;

                    // actualiza visualmente el boton
                    pintarBotonFavorito(false);
                });
    }

    // metodo para guardar o quitar un ejercicio de favoritos
    private void cambiarFavorito() {

        // valida que exista referencia de favorito
        if (favoritoRef == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        // si ya es favorito, se elimina
        if (esFavorito) {
            favoritoRef.removeValue()
                    .addOnSuccessListener(unused -> {

                        // actualiza el estado local
                        esFavorito = false;

                        // cambia el estilo del boton
                        pintarBotonFavorito(false);

                        // muestra mensaje de confirmacion
                        Toast.makeText(requireContext(), "Ejercicio quitado de favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->

                            // muestra mensaje si ocurre un error al quitar favorito
                            Toast.makeText(requireContext(), "Error al quitar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            // si no es favorito, se guarda el ejercicio completo
            favoritoRef.setValue(ejercicioActual)
                    .addOnSuccessListener(unused -> {
                        // actualiza el estado local
                        esFavorito = true;
                        // cambia el estilo del boton
                        pintarBotonFavorito(true);

                        // muestra mensaje de confirmacion
                        Toast.makeText(requireContext(), "Ejercicio guardado en favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            // muestra mensaje si ocurre un error al guardar favorito
                            Toast.makeText(requireContext(), "Error al guardar favorito: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    // metodo para cambiar el texto y estilo del boton de favorito
    private void pintarBotonFavorito(boolean favorito) {
        // si el ejercicio esta guardado como favorito
        if (favorito) {
            btnGuardarEjercicioDetalle.setText("★  Ejercicio guardado");
            btnGuardarEjercicioDetalle.setTextColor(ContextCompat.getColor(requireContext(), R.color.amarillo_estrella));
            btnGuardarEjercicioDetalle.setTypeface(null, Typeface.BOLD);
        } else {
            // si el ejercicio no esta guardado como favorito
            btnGuardarEjercicioDetalle.setText("☆  Guardar ejercicio");
            btnGuardarEjercicioDetalle.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_oscuro));
            btnGuardarEjercicioDetalle.setTypeface(null, Typeface.NORMAL);
        }
    }

    // metodo para obtener un identificador unico del ejercicio
    private String obtenerIdEjercicio(Ejercicio ejercicio) {
        // si el ejercicio tiene id numerico, se usa ese id
        if (ejercicio.getId() != 0) {
            return String.valueOf(ejercicio.getId());
        }

        // si no tiene id, se usa el nombre y la zona
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre() : "sin_nombre";
        String zona = ejercicio.getZona() != null ? ejercicio.getZona() : "sin_zona";

        // limpia el texto para que sea valido como clave de firebase
        return limpiarTextoParaFirebase(nombre + "_" + zona);
    }

    // metodo para limpiar caracteres que firebase no permite en las claves
    private String limpiarTextoParaFirebase(String texto) {

        // reemplaza caracteres no validos por guion bajo
        return texto.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_");
    }

    // metodo para devolver un texto seguro si el valor viene nulo o vacio
    private String valorSeguro(String texto, String valorPorDefecto) {

        // valida si el texto es nulo o esta vacio
        if (texto == null || texto.trim().isEmpty()) {

            // devuelve el valor por defecto
            return valorPorDefecto;
        }

        // devuelve el texto original si es valido
        return texto;
    }

    // metodo para crear instrucciones segun el ejercicio seleccionado
    private String[] crearInstrucciones(Ejercicio ejercicio) {

        // obtiene el nombre del ejercicio en minusculas
        String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";

        // obtiene la zona del ejercicio en minusculas
        String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";

        // obtiene la posicion del ejercicio en minusculas
        String posicion = ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";

        // instrucciones especificas para ejercicios de rodilla en posicion sentada
        if (posicion.contains("sentado") && (zona.contains("rodilla") || nombre.contains("rodilla"))) {
            return new String[]{
                    "Siéntate en una silla con la espalda recta y pies apoyados.",
                    "Extiende lentamente una pierna hasta que quede recta.",
                    "Mantén la posición durante 5 segundos respirando tranquilo.",
                    "Baja la pierna lentamente sin dejar caer.",
                    "Repite 10 veces con cada pierna."
            };
        }

        // instrucciones generales para ejercicios sentados
        if (posicion.contains("sentado")) {
            return new String[]{
                    "Siéntate en una silla estable.",
                    "Mantén la espalda recta y los pies apoyados.",
                    "Realiza el movimiento de forma lenta y controlada.",
                    "Evita forzar la zona trabajada.",
                    "Repite según las indicaciones del ejercicio."
            };
        }

        // instrucciones para ejercicios relacionados con rodilla
        if (zona.contains("rodilla") || nombre.contains("rodilla")) {
            return new String[]{
                    "Colócate en una posición cómoda y estable.",
                    "Realiza el movimiento de rodilla lentamente.",
                    "Mantén el movimiento suave y controlado.",
                    "Regresa poco a poco a la posición inicial.",
                    "Detente si sientes dolor intenso."
            };
        }

        // instrucciones para ejercicios relacionados con tobillo
        if (zona.contains("tobillo") || nombre.contains("tobillo")) {
            return new String[]{
                    "Apoya el pie de forma segura.",
                    "Realiza movimientos suaves con el tobillo.",
                    "Evita movimientos bruscos.",
                    "Regresa lentamente a la posición inicial.",
                    "Repite con cuidado."
            };
        }

        // instrucciones para ejercicios relacionados con hombro
        if (zona.contains("hombro") || nombre.contains("hombro")) {
            return new String[]{
                    "Mantén la espalda recta.",
                    "Relaja los hombros antes de iniciar.",
                    "Mueve el brazo lentamente.",
                    "No fuerces más allá de tu capacidad.",
                    "Regresa lentamente a la posición inicial."
            };
        }

        // instrucciones para ejercicios relacionados con espalda
        if (zona.contains("espalda") || nombre.contains("espalda")) {
            return new String[]{
                    "Mantén una postura cómoda y segura.",
                    "Realiza el movimiento lentamente.",
                    "Evita arquear demasiado la espalda.",
                    "Respira de forma controlada.",
                    "Detente si sientes dolor."
            };
        }

        // instrucciones generales si no se reconoce una zona especifica
        return new String[]{
                "Colócate en una posición cómoda.",
                "Realiza el movimiento lentamente.",
                "No fuerces tu cuerpo.",
                "Descansa si sientes molestia.",
                "Repite según las indicaciones."
        };
    }

    // metodo para obtener el icono segun la zona del ejercicio
    private int obtenerIconoZona(Ejercicio ejercicio) {

        // obtiene la zona del ejercicio en minusculas
        String zona = ejercicio.getZona().toLowerCase();

        // retorna icono de rodilla
        if (zona.contains("rodilla")) {
            return R.drawable.rodilla;
        }

        // retorna icono de pierna
        if (zona.contains("pierna")) {
            return R.drawable.pierna;
        }

        // retorna icono de brazo para hombro
        if (zona.contains("hombro")) {
            return R.drawable.brazo;
        }

        // retorna icono de tobillo
        if (zona.contains("tobillo")) {
            return R.drawable.tobillo;
        }

        // retorna icono de espalda
        if (zona.contains("espalda")) {
            return R.drawable.espalda;
        }

        // retorna icono de mano
        if (zona.contains("mano")) {
            return R.drawable.mano;
        }

        // retorna icono de musculo para brazo
        if (zona.contains("brazo")) {
            return R.drawable.musculo;
        }

        // retorna icono de muneca
        if (zona.contains("muñeca") || zona.contains("muneca")) {
            return R.drawable.muneca;
        }

        // retorna icono por defecto
        return R.drawable.musculo;
    }

    // metodo para ocultar la barra inferior de navegacion
    private void ocultarBottomNavigation() {

        // valida que la actividad exista
        if (getActivity() != null) {

            // obtiene la barra inferior desde la actividad
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);

            // valida que la barra exista
            if (bottomNavigationView != null) {

                // oculta la barra inferior
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    // metodo para mostrar nuevamente la barra inferior de navegacion
    private void mostrarBottomNavigation() {

        // valida que la actividad exista
        if (getActivity() != null) {

            // obtiene la barra inferior desde la actividad
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigation);

            // valida que la barra exista
            if (bottomNavigationView != null) {

                // muestra la barra inferior
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // vuelve a mostrar la barra inferior al salir del detalle
        mostrarBottomNavigation();
    }
}