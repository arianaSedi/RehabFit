package com.example.rehabfit.network;
import com.example.rehabfit.models.Ejercicio;
import com.example.rehabfit.models.EjercicioResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface ApiService {

    @GET("ejercicios")
    Call<EjercicioResponse> obtenerEjercicios();

    @GET("ejercicios/{id}")
    Call<Ejercicio> obtenerEjercicioPorId(@Path("id") int id);

    @GET("ejercicios/zona/{zona}")
    Call<EjercicioResponse> obtenerEjerciciosPorZona(@Path("zona") String zona);

    @GET("ejercicios/nivel/{nivel}")
    Call<EjercicioResponse> obtenerEjerciciosPorNivel(@Path("nivel") String nivel);

    @GET("buscar")
    Call<EjercicioResponse> buscarEjercicios(@Query("texto") String texto);
}
