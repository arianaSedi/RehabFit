package com.example.rehabfit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rehabfit.DetallePublicacionActivity;
import com.example.rehabfit.PublicarComunidadActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.PublicacionAdapter;
import com.example.rehabfit.models.PublicacionComunidad;
import com.google.firebase.database.*;

        import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComunidadFragment extends Fragment {

    private RecyclerView rvPublicaciones;
    private Button btnPublicar;

    private PublicacionAdapter adapter;
    private List<PublicacionComunidad> listaPublicaciones;

    private DatabaseReference refPublicaciones;

    public ComunidadFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_comunidad, container, false);

        rvPublicaciones = vista.findViewById(R.id.rvPublicaciones);
        btnPublicar = vista.findViewById(R.id.btnPublicar);

        listaPublicaciones = new ArrayList<>();

        adapter = new PublicacionAdapter(listaPublicaciones, publicacion -> {
            Intent intent = new Intent(requireContext(), DetallePublicacionActivity.class);
            intent.putExtra("idPublicacion", publicacion.getId());
            startActivity(intent);
        });

        rvPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPublicaciones.setAdapter(adapter);

        refPublicaciones = FirebaseDatabase.getInstance().getReference("publicacionesComunidad");

        btnPublicar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PublicarComunidadActivity.class);
            startActivity(intent);
        });

        cargarPublicaciones();

        return vista;
    }

    private void cargarPublicaciones() {
        refPublicaciones.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaPublicaciones.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            PublicacionComunidad publicacion = data.getValue(PublicacionComunidad.class);

                            if (publicacion != null) {
                                listaPublicaciones.add(publicacion);
                            }
                        }

                        Collections.reverse(listaPublicaciones);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}