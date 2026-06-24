package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.adapters.FiltroAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.EjercicioAdapter;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.models.EjercicioResponse;
import com.example.rehabfit.network.ApiService;
import com.example.rehabfit.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EjerciciosFragment extends Fragment {

    private RecyclerView rvEjercicios;
    private EditText edtBuscarEjercicio;

    private RecyclerView rvFiltros;
    private FiltroAdapter filtroAdapter;
    private ArrayList<String> filtros;

    private EjercicioAdapter adapter;

    private final List<Ejercicio> listaCompleta = new ArrayList<>();
    private final List<Ejercicio> listaFiltrada = new ArrayList<>();

    private Call<EjercicioResponse> callEjercicios;

    private String filtroSeleccionado = "Todos";

    public EjerciciosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_ejercicios, container, false);

        // vincula los componentes visuales del layout
        rvEjercicios = vista.findViewById(R.id.rvEjercicios);
        edtBuscarEjercicio = vista.findViewById(R.id.edtBuscarEjercicio);
        rvFiltros = vista.findViewById(R.id.rvFiltros);

        // configura el recycler view de filtros de forma horizontal
        rvFiltros.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // crea la lista de filtros disponibles
        filtros = new ArrayList<>();

        // agrega las categorias que se mostraran como filtros
        filtros.add("Todos");
        filtros.add("Brazo");
        filtros.add("Hombro");
        filtros.add("Muñeca");
        filtros.add("Mano");
        filtros.add("Espalda");
        filtros.add("Rodilla");
        filtros.add("Pierna");
        filtros.add("Tobillo");
        filtros.add("Sentado");

        // crea el adaptador de filtros y define que hacer al seleccionar uno
        filtroAdapter = new FiltroAdapter(filtros, filtro -> {

            // si selecciona todos, se muestran todos los ejercicios
            if (filtro.equalsIgnoreCase("Todos")) {
                mostrarTodos();

            } else {

                // si selecciona una categoria, se filtra por esa categoria
                filtrarPorCategoria(filtro);
            }
        });

        // asigna el adaptador de filtros al recycler view
        rvFiltros.setAdapter(filtroAdapter);

        // configura el recycler view de ejercicios en forma de lista vertical
        rvEjercicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        // crea el adaptador de ejercicios usando la lista filtrada
        adapter = new EjercicioAdapter(listaFiltrada);

        // asigna el adaptador al recycler view de ejercicios
        rvEjercicios.setAdapter(adapter);

        // configura el buscador de ejercicios
        configurarBuscador();

        // actualiza visualmente el filtro seleccionado
        actualizarFiltro();

        // carga los ejercicios desde la api
        cargarEjerciciosAPI();

        // devuelve la vista del fragment
        return vista;
    }

    // metodo encargado de cargar los ejercicios desde la api
    private void cargarEjerciciosAPI() {

        // crea el servicio de la api usando retrofit
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // prepara la llamada para obtener ejercicios
        callEjercicios = apiService.obtenerEjercicios();

        // ejecuta la llamada de forma asincrona
        callEjercicios.enqueue(new Callback<EjercicioResponse>() {
            @Override
            public void onResponse(Call<EjercicioResponse> call, Response<EjercicioResponse> response) {

                // valida que el fragment siga activo
                if (!isAdded()) {
                    return;
                }

                // valida que la respuesta sea correcta y tenga datos
                if (response.isSuccessful() && response.body() != null) {

                    // limpia las listas antes de agregar nuevos datos
                    listaCompleta.clear();
                    listaFiltrada.clear();

                    // agrega todos los ejercicios recibidos desde la api
                    listaCompleta.addAll(response.body().getEjercicios());

                    // copia todos los ejercicios a la lista filtrada
                    listaFiltrada.addAll(listaCompleta);

                    // actualiza el recycler view
                    adapter.notifyDataSetChanged();

                    // aplica la zona afectada guardada en el perfil del usuario
                    aplicarZonaGuardada();

                } else {

                    // muestra mensaje si la api no devuelve datos correctamente
                    Toast.makeText(requireContext(), "No se pudieron cargar los ejercicios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EjercicioResponse> call, Throwable t) {

                // valida que el fragment siga activo y que la llamada no haya sido cancelada
                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                // muestra mensaje si ocurre un error de conexion
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // metodo encargado de configurar el buscador
    private void configurarBuscador() {

        // escucha los cambios en el texto escrito por el usuario
        edtBuscarEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // filtra los ejercicios segun el texto escrito
                filtrarPorTexto(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // metodo para mostrar todos los ejercicios
    private void mostrarTodos() {

        // guarda todos como filtro seleccionado
        filtroSeleccionado = "Todos";

        // actualiza el estilo visual del filtro
        actualizarFiltro();

        // limpia el buscador
        edtBuscarEjercicio.setText("");

        // limpia la lista filtrada
        listaFiltrada.clear();

        // agrega todos los ejercicios disponibles
        listaFiltrada.addAll(listaCompleta);

        // actualiza la lista en pantalla
        adapter.notifyDataSetChanged();
    }

    // metodo para filtrar ejercicios por categoria
    private void filtrarPorCategoria(String categoria) {

        // guarda la categoria seleccionada
        filtroSeleccionado = categoria;

        // actualiza el estilo visual del filtro
        actualizarFiltro();

        // limpia el campo de busqueda
        edtBuscarEjercicio.setText("");

        // limpia la lista filtrada
        listaFiltrada.clear();

        // recorre todos los ejercicios cargados
        for (Ejercicio ejercicio : listaCompleta) {

            // valida si la zona del ejercicio coincide con la categoria
            boolean coincideZona = ejercicio.getZona() != null &&
                    ejercicio.getZona().equalsIgnoreCase(categoria);

            // valida si la posicion del ejercicio coincide con la categoria
            boolean coincidePosicion = ejercicio.getPosicion() != null &&
                    ejercicio.getPosicion().equalsIgnoreCase(categoria);

            // agrega el ejercicio si coincide en zona o posicion
            if (coincideZona || coincidePosicion) {
                listaFiltrada.add(ejercicio);
            }
        }

        // actualiza el recycler view
        adapter.notifyDataSetChanged();
    }

    // metodo para filtrar ejercicios segun el texto del buscador
    private void filtrarPorTexto(String texto) {

        // convierte el texto a minusculas y elimina espacios extras
        String busqueda = texto.toLowerCase().trim();

        // si hay texto, mantiene actualizado el filtro visual
        if (!busqueda.isEmpty()) {
            actualizarFiltro();
        }

        // limpia la lista filtrada
        listaFiltrada.clear();

        // si no hay texto, muestra todos los ejercicios
        if (busqueda.isEmpty()) {
            listaFiltrada.addAll(listaCompleta);

        } else {

            // recorre todos los ejercicios disponibles
            for (Ejercicio ejercicio : listaCompleta) {

                // obtiene los campos del ejercicio evitando valores nulos
                String nombre = ejercicio.getNombre() != null ? ejercicio.getNombre().toLowerCase() : "";
                String zona = ejercicio.getZona() != null ? ejercicio.getZona().toLowerCase() : "";
                String nivel = ejercicio.getNivel() != null ? ejercicio.getNivel().toLowerCase() : "";
                String posicion = ejercicio.getPosicion() != null ? ejercicio.getPosicion().toLowerCase() : "";
                String descripcion = ejercicio.getDescripcion() != null ? ejercicio.getDescripcion().toLowerCase() : "";

                // valida si algun campo contiene el texto buscado
                if (nombre.contains(busqueda) || zona.contains(busqueda) || nivel.contains(busqueda) || posicion.contains(busqueda) || descripcion.contains(busqueda)) {

                    // agrega el ejercicio si coincide con la busqueda
                    listaFiltrada.add(ejercicio);
                }
            }
        }

        // actualiza el recycler view
        adapter.notifyDataSetChanged();
    }

    // metodo para actualizar visualmente el filtro seleccionado
    private void actualizarFiltro() {

        // valida que el adaptador de filtros exista
        if (filtroAdapter != null) {

            // envia el filtro seleccionado al adaptador
            filtroAdapter.setFiltroSeleccionado(
                    filtroSeleccionado);
        }
    }

    // metodo para aplicar automaticamente la zona afectada guardada en el perfil
    private void aplicarZonaGuardada() {

        // valida si hay un usuario autenticado
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        // obtiene el uid del usuario actual
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        // busca la zona afectada en el perfil adaptado del usuario
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("perfilAdaptado")
                .child("zonaAfectada")
                .get()
                .addOnSuccessListener(snapshot -> {

                    // obtiene la zona guardada
                    String zona = snapshot.getValue(String.class);

                    // si no existe zona guardada, no aplica filtro
                    if (zona == null || zona.isEmpty()) {
                        return;
                    }

                    // filtra los ejercicios segun la zona guardada
                    filtrarPorCategoria(zona);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // cancela la llamada a la api si sigue activa
        if (callEjercicios != null) {
            callEjercicios.cancel();
        }

        // libera el listener de favoritos para evitar fugas de memoria
        if (adapter != null) {
            adapter.liberarListenerFavoritos();
        }
    }
}