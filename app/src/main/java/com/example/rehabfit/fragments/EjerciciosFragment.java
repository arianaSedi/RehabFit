package com.example.rehabfit.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rehabfit.R;


public class EjerciciosFragment extends Fragment {


    public EjerciciosFragment() {
        // Required empty public constructor
    }

    public static EjerciciosFragment newInstance(String param1, String param2) {
        EjerciciosFragment fragment = new EjerciciosFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ejercicios, container, false);
    }
}