package com.example.rehabfit.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rehabfit.MainActivity;
import com.example.rehabfit.R;
import com.example.rehabfit.adapters.RutinaAdapter;
import com.example.rehabfit.utils.RutinaManager;


public class RutinaFragment extends Fragment {

    private TextView txtResumenRutina;
    private RecyclerView rvRutina;
    private AppCompatButton btnIniciarRutina;
    private AppCompatButton btnAgregarEjercicio;

    private RutinaAdapter adapter;

    public RutinaFragment() {
        // Required empty public constructor
    }
    public static RutinaFragment newInstance(String param1, String param2) {
        RutinaFragment fragment = new RutinaFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_rutina, container, false);

        txtResumenRutina = vista.findViewById(R.id.txtResumenRutina);
        rvRutina = vista.findViewById(R.id.rvRutina);
        btnIniciarRutina = vista.findViewById(R.id.btnIniciarRutina);
        btnAgregarEjercicio = vista.findViewById(R.id.btnAgregarEjercicio);

        rvRutina.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RutinaAdapter(RutinaManager.obtenerRutina(), this::actualizarResumen);
        rvRutina.setAdapter(adapter);

        actualizarResumen();

        btnIniciarRutina.setOnClickListener(v -> {
            if (RutinaManager.obtenerRutina().isEmpty()) {
                Toast.makeText(requireContext(), "Primero agrega ejercicios a tu rutina", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Rutina iniciada próximamente", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarEjercicio.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).cambiarFragmentBoton(R.id.nav_ejercicios);
            }
        });

        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        actualizarResumen();
    }

    private void actualizarResumen() {
        int cantidad = RutinaManager.obtenerRutina().size();
        int minutos = RutinaManager.obtenerTotalMinutos();

        txtResumenRutina.setText(cantidad + " ejercicios · " + minutos + " minutos");
    }
}
