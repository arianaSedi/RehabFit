package com.example.rehabfit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.rehabfit.fragments.ComunidadFragment;
import com.example.rehabfit.fragments.EjerciciosFragment;
import com.example.rehabfit.fragments.InicioFragment;
import com.example.rehabfit.fragments.PerfilFragment;
import com.example.rehabfit.fragments.RutinaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                cargarFragment(new InicioFragment());
                return true;
            }

            if (id == R.id.nav_ejercicios) {
                cargarFragment(new EjerciciosFragment());
                return true;
            }

            if (id == R.id.nav_rutina) {
                cargarFragment(new RutinaFragment());
                return true;
            }

            if (id == R.id.nav_comunidad) {
                cargarFragment(new ComunidadFragment());
                return true;
            }

            if (id == R.id.nav_perfil) {
                cargarFragment(new PerfilFragment());
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_inicio);
        }
    }

    public void cambiarFragmentBoton(int idMenu) {
        bottomNavigation.setSelectedItemId(idMenu);
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }
}