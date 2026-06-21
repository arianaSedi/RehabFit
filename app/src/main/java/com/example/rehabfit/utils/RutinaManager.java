package com.example.rehabfit.utils;

import com.example.rehabfit.models.Ejercicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class RutinaManager {
    private static final List<Ejercicio> ejerciciosRutina = new ArrayList<>();
    public interface RutinaCallback {
        void onRutinaCargada();
    }
    public interface AccionCallback {
        void onExito();
        void onError(String error);
    }

    public interface AgregarCallback {
        void onAgregado();
        void onYaExistia();
        void onError(String error);
    }

    //callback para devolver datos resumen al inicio
    public interface ResumenCallback {
        void onResumen(int sesionesSemana, int minutosTotales);
    }

    //se obtiene el uid del usuario autenticado
    private static String obtenerUid() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return null;
        }

        return usuario.getUid();
    }

    private static DatabaseReference obtenerReferenciaUsuario() {

        String uid = obtenerUid();

        if (uid == null) {
            return null;
        }

        return FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
    }

    //carga la rutina guardada en firebase
    public static void cargarRutinaActual(RutinaCallback callback) {  //Un callback es una forma de que un metodo me devuelva una respuesta cuando termina de hacer algo

        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            ejerciciosRutina.clear();

            if (callback != null) {
                callback.onRutinaCargada();
            }
            return;
        }

        usuarioRef.child("rutina").get().addOnSuccessListener(snapshot -> {

            // limpia la lista para evitar duplicados
            ejerciciosRutina.clear();

            for (DataSnapshot item : snapshot.getChildren()) {
                Ejercicio ejercicio = item.getValue(Ejercicio.class);

                if (ejercicio != null) {
                    ejerciciosRutina.add(ejercicio);
                }
            }

            if (callback != null) {
                callback.onRutinaCargada();
            }

        }).addOnFailureListener(e -> {
            if (callback != null) {
                callback.onRutinaCargada();
            }
        });
    }

    // agrega un ejercicio a la rutina
    public static void agregarEjercicio(Ejercicio ejercicio, AccionCallback callback) {

        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            if (callback != null) {
                callback.onError("No hay usuario activo");
            }
            return;
        }

        // verifica si el ejercicio ya existe en la rutina
        for (Ejercicio item : ejerciciosRutina) {

            if (item.getId() == ejercicio.getId()) {
                if (callback != null) {
                    callback.onExito();
                }
                return;
            }
        }

        String idEjercicio = String.valueOf(ejercicio.getId());

        usuarioRef.child("rutina").child(idEjercicio).setValue(ejercicio).addOnSuccessListener(unused -> {
            ejerciciosRutina.add(ejercicio);

            if (callback != null) {
                callback.onExito();
            }

        }).addOnFailureListener(e -> {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }

    //se agrega un ejercicio validando si ya existe
    public static void agregarEjercicioConValidacion(Ejercicio ejercicio, AgregarCallback callback) {
        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            if (callback != null) {
                callback.onError("No hay usuario activo");
            }
            return;
        }

        //evita guardar ejercicios repetidos
        for (Ejercicio item : ejerciciosRutina) {

            if (item.getId() == ejercicio.getId()) {
                if (callback != null) {
                    callback.onYaExistia();
                }
                return;
            }
        }

        String idEjercicio = String.valueOf(ejercicio.getId());

        usuarioRef.child("rutina").child(idEjercicio).setValue(ejercicio).addOnSuccessListener(unused -> {

            ejerciciosRutina.add(ejercicio);

            if (callback != null) {
                callback.onAgregado();
            }
        }).addOnFailureListener(e -> {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }

    public static void eliminarEjercicio(Ejercicio ejercicio, AccionCallback callback) {

        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            if (callback != null) {
                callback.onError("No hay usuario activo");
            }
            return;
        }

        String idEjercicio = String.valueOf(ejercicio.getId());

        usuarioRef.child("rutina").child(idEjercicio).removeValue().addOnSuccessListener(unused -> {

            // tambien se elimina de la lista local
            for (int i = 0; i < ejerciciosRutina.size(); i++) {

                if (ejerciciosRutina.get(i).getId() == ejercicio.getId()) {
                    ejerciciosRutina.remove(i);
                    break;
                }
            }
            if (callback != null) {
                callback.onExito();
            }
        }).addOnFailureListener(e -> {

            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }

    //devuelve la lista actual de ejercicios
    public static List<Ejercicio> obtenerRutina() {
        return ejerciciosRutina;
    }

    //calcula los minutos totales de la rutina
    public static int obtenerTotalMinutos() {
        int total = 0;

        for (Ejercicio ejercicio : ejerciciosRutina) {
            total += ejercicio.getDuracionMinutos();
        }
        return total;
    }

    public static void guardarSesionTerminada(int minutosTotales, int cantidadEjercicios, int dolorAntes, int dolorDespues, String zonaPrincipal, AccionCallback callback) {

        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            if (callback != null) {
                callback.onError("No hay usuario activo");
            }
            return;
        }

        HashMap<String, Object> sesion = new HashMap<>();

        sesion.put("fechaMillis", System.currentTimeMillis());
        sesion.put("minutosTotales", minutosTotales);
        sesion.put("cantidadEjercicios", cantidadEjercicios);
        sesion.put("dolorAntes", dolorAntes);
        sesion.put("dolorDespues", dolorDespues);
        sesion.put("zonaPrincipal", zonaPrincipal);

        usuarioRef.child("sesiones").push().setValue(sesion).addOnSuccessListener(unused -> {

            if (callback != null) {
                callback.onExito();
            }
        }).addOnFailureListener(e -> {

            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }

    //carga info resumen para la pantalla de inicio
    public static void cargarResumenInicio(ResumenCallback callback) {

        DatabaseReference usuarioRef = obtenerReferenciaUsuario();

        if (usuarioRef == null) {
            if (callback != null) {
                callback.onResumen(0, 0);
            }
            return;
        }

        usuarioRef.child("sesiones").get().addOnSuccessListener(snapshot -> {

            int sesionesSemana = 0;
            int minutosTotales = 0;

            Calendar inicioSemana = Calendar.getInstance();
            int diaActual = inicioSemana.get(Calendar.DAY_OF_WEEK);
            int diferencia;

            // calcula el inicio de la semana actual
            if (diaActual == Calendar.SUNDAY) {
                diferencia = -6;
            } else {
                diferencia = Calendar.MONDAY - diaActual;
            }

            inicioSemana.add(Calendar.DAY_OF_MONTH, diferencia);
            inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
            inicioSemana.set(Calendar.MINUTE, 0);
            inicioSemana.set(Calendar.SECOND, 0);
            inicioSemana.set(Calendar.MILLISECOND, 0);

            long inicioSemanaMillis = inicioSemana.getTimeInMillis();

            for (DataSnapshot item : snapshot.getChildren()) {
                Long fechaMillis = item.child("fechaMillis").getValue(Long.class);
                Integer minutos = item.child("minutosTotales").getValue(Integer.class);

                if (minutos != null) {
                    minutosTotales += minutos;
                }

                // cuenta cuantas sesiones se hicieron desde el inicio de la semana actual
                if (fechaMillis != null && fechaMillis >= inicioSemanaMillis) {
                    sesionesSemana++;
                }
            }

            if (callback != null) {
                callback.onResumen(
                        sesionesSemana,
                        minutosTotales
                );
            }
        }).addOnFailureListener(e -> {
            if (callback != null) {
                callback.onResumen(0, 0);
            }
        });
    }

    public static void limpiarRutinaLocal() {
        ejerciciosRutina.clear();
    }
}