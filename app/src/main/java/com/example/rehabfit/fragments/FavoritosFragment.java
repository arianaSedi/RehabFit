package com.example.rehabfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rehabfit.R;
import com.example.rehabfit.adapters.EjercicioAdapter;
import com.example.rehabfit.models.Ejercicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavoritosFragment extends Fragment {

    private RecyclerView rvFavoritos;
    private TextView txtSinFavoritos;
    private ImageButton btnVolverFavoritos;

    private EjercicioAdapter adapter;
    private final List<Ejercicio> listaFavoritos = new ArrayList<>();

    private DatabaseReference favoritosRef;
    private ValueEventListener favoritosListener;

    public FavoritosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_favoritos, container, false);
        //declaración de variables
        rvFavoritos = vista.findViewById(R.id.rvFavoritos);
        txtSinFavoritos = vista.findViewById(R.id.txtSinFavoritos);
        btnVolverFavoritos = vista.findViewById(R.id.btnVolverFavoritos);
    //indica que nuestros elementos apareceran verticalmente
        rvFavoritos.setLayoutManager(new LinearLayoutManager(requireContext()));
        //adaptador usando la lista de fav
        adapter = new EjercicioAdapter(listaFavoritos);
        rvFavoritos.setAdapter(adapter);

        btnVolverFavoritos.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    //lee lo que tenemos en nuestra firebase
        cargarFavoritos();

        return vista;
    }
    private void cargarFavoritos() {
        //obtiene el usuario que inicia sesion
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
      //si no hay usuario registrado pide que se inicie sesion
        if (usuarioActual == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            mostrarVacio();
            return;
        }
    //BUSCA LOS FAVORITOS QUE TENEMOS AGREGADOS EN NUESTRA FIREBASE SEGUN CADA USUARIO
        favoritosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuarioActual.getUid())
                .child("favoritos");
//ACA BUSCA O VE LOS CAMBIOS EN NUETSRO FIREBASE SI HAY O NO HAY FAVORITOS
        favoritosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //BORRA LOS DATOS VIEJOS
                listaFavoritos.clear();
        //RECORRE LA LISTA DE FAVORITOS QUE TEEMOS DE CADA USUARIO EN FIREBASE
                for (DataSnapshot item : snapshot.getChildren()) {
                    //FIREBASE NOS DEVUELVE UN JSON.
                    Ejercicio ejercicio = item.getValue(Ejercicio.class);

                    if (ejercicio != null) {
                        //ACA SE LLENA LA LISTA CON LOS EJERCICIOS SELECCIONADOS COMO FAVORITOS
                        listaFavoritos.add(ejercicio);
                    }
                }
            //REDIBUJAR LA LISTA DE LOS FAVORITOS SI ESTOS CAMBIAN
                adapter.notifyDataSetChanged();
    //SI HAY FAVORITOS LOS MUESTRA, SI NO LA LISTA ESTARA VACIA Y POR ENDE NO MUESTRA NADA
                if (listaFavoritos.isEmpty()) {
                    mostrarVacio();
                } else {
                    txtSinFavoritos.setVisibility(View.GONE);
                    rvFavoritos.setVisibility(View.VISIBLE);
                }
            }
            //se ejecutara si nuestro firebase falla ya sea por internet, por errores de lectura
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
            }
        };
        favoritosRef.addValueEventListener(favoritosListener);
    }

    private void mostrarVacio() {
        txtSinFavoritos.setVisibility(View.VISIBLE);
        rvFavoritos.setVisibility(View.GONE);
    }
    @Override
    //se ejecuta cuando salimos del fragment
    public void onDestroyView() {
        super.onDestroyView();

        if (favoritosRef != null && favoritosListener != null) {
            favoritosRef.removeEventListener(favoritosListener);
        }

        if (adapter != null) {
            adapter.liberarListenerFavoritos();
        }
    }
}