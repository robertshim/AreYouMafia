package com.joycity.intern.areyoumafia;

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApplicationController extends Application {
    private NetworkService networkService;
    private String sessionkey;
    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.255.252.84:8080/mafia/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public NetworkService getNetworkService() {
        return networkService;
    }

    public void setSessionkey(String sessionkey) {
        this.sessionkey = sessionkey;
    }

    public String getSessionkey() {
        return sessionkey;
    }
}

