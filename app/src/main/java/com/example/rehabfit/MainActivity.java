package com.example.rehabfit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        //detecta la opcion seleccionada por el usuario
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

        //al iniciar la aplicacion muestra la pantalla de inicio
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_inicio);
        }
    }

    //metodo para cambiar de fragmento desde otra clase usando el menu
    public void cambiarFragmentBoton(int idMenu) {
        bottomNavigation.setVisibility(android.view.View.VISIBLE);
        bottomNavigation.setSelectedItemId(idMenu);  // selecciona la opcion indicada del menu
    }

    //metodo que reemplaza el fragmento actual por otro dentro del contenedor
    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }
}