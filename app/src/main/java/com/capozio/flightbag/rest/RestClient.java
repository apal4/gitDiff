package com.capozio.flightbag.rest;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/*** ***********************************************************************
 * <p>
 * Pilot Training System CONFIDENTIAL
 * __________________
 * <p>
 * [2015] - [2017] Pilot Training System
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of Pilot Training System,
 * The intellectual and technical concepts contained
 * herein are proprietary to Pilot Training System
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Pilot Training System.
 *
 * Created by Ying Zhang on 9/9/16.
 *
 *
 */

/**
 * Retrofit Instance for handling HTTP requests.
 */
// TODO: hide credentials
public class RestClient {
    private static final String CLOUDANT_BASE_URL = "https://jkoch.cloudant.com/";
    private static Retrofit retrofit = null;

    private RestClient(){}

    public static Retrofit getInstance() {
        if(retrofit == null)
            return getClient();
        return retrofit;
    }

    private static Retrofit getClient() {

        // add logger for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        //set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // add interceptor to add Authorization header to the HTTP request
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", Credentials.basic("jkoch", "Kochie666"))
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        // add logging as last interceptors
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(CLOUDANT_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit;
    }
}
