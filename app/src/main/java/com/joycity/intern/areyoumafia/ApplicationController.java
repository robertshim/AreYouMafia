package com.joycity.intern.areyoumafia;

import android.app.Application;

import retrofit2.Retrofit;

public class ApplicationController extends Application {
    private NetworkService networkService;
    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
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
}
