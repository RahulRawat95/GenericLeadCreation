package com.wings2aspirations.genericleadcreation.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static String BASE_URL;

    public static String dbName;

    public static String schemaName;

    public static String authString;

    public static String applicationId;

    public static Retrofit client;

    public static int id;

    public static String getDbName() {
        return dbName;
    }

    public static String getSchemaName() {
        return schemaName;
    }

    public static void setDbName(String dbName) {
        ApiClient.dbName = dbName;
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }

    public static void setAuthString(String authString) {
        ApiClient.authString = authString;
    }

    public static void setId(int id) {
        ApiClient.id = id;
    }

    public static void setSchemaName(String schemaName) {
        ApiClient.schemaName = schemaName;
    }

    public static void setApplicationId(String applicationId) {
        ApiClient.applicationId = applicationId;
    }

    public static Retrofit getClient() {
        if (client == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    String databaseName = dbName;
                    String authorisationString = authString == null ? "asdasdasdad" : authString;
                    HttpUrl url = original.url().newBuilder()
                            .addQueryParameter("dbName", databaseName)
                            .addQueryParameter("schemaName", schemaName)
                            .build();

                    Request request = original.newBuilder()
                            .url(url)
                            .header("authorization", authorisationString)
                            .method(original.method(), original.body())
                            .build();

                    Log.d("intercept", "intercept: " + request.url() + "\n" + authorisationString + "\n" + databaseName);
                    return chain.proceed(request);
                }
            });

            OkHttpClient okHttpClient = httpClient.build();
            client = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return client;
    }

}
