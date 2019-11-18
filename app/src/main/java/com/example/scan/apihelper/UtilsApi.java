package com.example.scan.apihelper;

public class UtilsApi {

    // 10.0.2.2 ini adalah localhost.https://cobabioskop.000webhostapp.com/
    public static final String BASE_URL_API = "http://192.168.8.109/admin-api/";

    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClient.getClient(BASE_URL_API).create(BaseApiService.class);
    }
}
