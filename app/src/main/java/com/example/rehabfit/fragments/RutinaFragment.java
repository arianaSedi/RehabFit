package com.example.rehabfit.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rehabfit.R;


public class RutinaFragment extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rutina, container, false);
    }
}