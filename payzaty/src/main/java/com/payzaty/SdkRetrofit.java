package com.payzaty;

import com.payzaty.payment.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


class SdkRetrofit {

    // TODO LOCK for Sdk Service
    private static final Object LOCK = new Object();


    private Retrofit retrofit;
    private SdkService sdkService;

    private SdkRetrofit() {

    }

    public static SdkRetrofit getInstance(boolean sandbox) {
        synchronized (LOCK) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.SECONDS)
                    .writeTimeout(10000, TimeUnit.SECONDS)
                    .readTimeout(10000, TimeUnit.SECONDS)
                    .build();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            SdkRetrofit instance = new SdkRetrofit();
            instance.retrofit = new Retrofit.Builder()
                    .baseUrl(sandbox ? BuildConfig.BaseSandboxUrl : BuildConfig.BaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return instance;
        }
    }


    public SdkService getSdkService() {
        synchronized (LOCK) {
            if (sdkService == null) {
                sdkService = retrofit.create(SdkService.class);
            }
            return sdkService;
        }
    }

}
