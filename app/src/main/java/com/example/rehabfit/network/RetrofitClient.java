package com.example.rehabfit.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// clase encargada de configurar y proporcionar una unica instancia de retrofit
public class RetrofitClient {
    // url base de la api utilizada por la aplicacion
    private static final String BASE_URL = "https://rehabfitapi.onrender.com/api/";
    // instancia unica de retrofit
    private static Retrofit retrofit;
    // metodo encargado de devolver la instancia de retrofit
    public static Retrofit getClient() {
        // verifica si retrofit aun no ha sido creado
        if (retrofit == null) {
            // configura el cliente http con tiempos de espera personalizados
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // tiempo maximo para establecer conexion con el servidor
                    .connectTimeout(60, TimeUnit.SECONDS)
                    // tiempo maximo para recibir respuesta del servidor
                    .readTimeout(60, TimeUnit.SECONDS)
                    // tiempo maximo para enviar datos al servidor
                    .writeTimeout(60, TimeUnit.SECONDS)
                    // construye el cliente http
                    .build();
// crea la instancia de retrofit
            retrofit = new Retrofit.Builder()
                    // establece la url base de la api
                    .baseUrl(BASE_URL)
                    // asigna el cliente http configurado
                    .client(okHttpClient)
                    // agrega gson para convertir respuestas json en objetos java
                    .addConverterFactory(GsonConverterFactory.create())
                    // construye la instancia de retrofit
                    .build();
        }
// devuelve la instancia creada o existente
        return retrofit;
    }
}