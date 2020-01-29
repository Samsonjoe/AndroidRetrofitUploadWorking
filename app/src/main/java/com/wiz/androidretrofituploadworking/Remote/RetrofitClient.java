package com.wiz.androidretrofituploadworking.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.wiz.androidretrofituploadworking.Utils.RestSSLCertificateAdapter.getUnsafeOkHttpClient;

public class RetrofitClient {

    private static Retrofit retrofitClient = null;

    public static Retrofit getClient(String baseUrl)
    {
        if (retrofitClient == null)
        {
            retrofitClient = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofitClient;
    }
}
