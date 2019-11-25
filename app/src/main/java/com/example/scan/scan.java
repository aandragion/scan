package com.example.scan;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scan.apihelper.BaseApiService;
import com.example.scan.apihelper.UtilsApi;
import com.example.scan.apihelper.list_kursi;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;

public class scan extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    public String scanResult;
    public String id_pesan, keyVal, key, key1;

    private BaseApiService apiInterface;
    private static final int REQUEST_CAMERA = 1;
    ProgressDialog loading;
    private Dialog customDialog;
    Context mContext;
    BaseApiService mApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        mContext = this;
        mApiService = UtilsApi.getAPIService();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (checkPermission()) {
            } else {
                requestPermission();
            }
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(scan.this, CAMERA) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }


    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(scan.this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        mScannerView.resumeCameraPreview(this);
        scanResult = result.getText();
        requestScan(scanResult);
    }

    private void requestScan(String hasil_scan) {
        loading = ProgressDialog.show(scan.this, null, "Harap Tunggu...", true, false);
        mApiService.scanRequest(hasil_scan)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            loading.dismiss();
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("error").equals("false")) {
                                    String success_msg = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, success_msg, Toast.LENGTH_SHORT).show();
//                                    id_pesan = jsonRESULTS.getJSONObject("user").getString("kursi");
//                                    final String[] kursi = id_pesan.split("\\s+");
//                                    final boolean[] checkedkursi = new boolean[kursi.length];
//                                    final boolean status_checked[] = new boolean[kursis.length()];

                                    JSONArray arr = jsonRESULTS.getJSONArray("dipesan");
                                    {
                                        for (int i = 0; i < arr.length(); i++) {
                                            key = arr.getJSONObject(i).getString("kursi");
//                                            Toast.makeText(mContext, "dipesan " + key, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    JSONArray arr1 = jsonRESULTS.getJSONArray("ditempati"); {
                                        for (int j = 0; j < arr1.length(); j++) {
                                            key1 = arr1.getJSONObject(j).getString("s_kursi");
//                                            Toast.makeText(mContext, " ditempati" + key1, Toast.LENGTH_SHORT).show();
                                        }
                                    }
//                                    final String dipesan[] = {key};
                                    final boolean status_checked[] = new boolean[key.length()];
                                    AlertDialog.Builder builder = new AlertDialog.Builder(scan.this);
                                    builder.setTitle("Scan Result");
//                                    Toast.makeText(mContext, "dipesan " + dipesan, Toast.LENGTH_SHORT).show();
                                    for (int i=0; i< key.length(); i++) {
                                        for ( int j=0; j< key1.length(); j++ ) {
                                            if (key.equals(key1)) {// Status checked diubah ke true;
                                                status_checked[i] = true;
                                            }
                                        }


                                        builder.setMultiChoiceItems(new String[]{key}, status_checked, new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                                                        checkedkursi[which] = isChecked;
                                                    }
                                                }
                                        );

                                    }
//                                    kursis = jsonRESULTS.getJSONObject("user");

//                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            StringBuilder stringBuilder = new StringBuilder();
//                                            for (int i = 0; i < kursi.length; i++) {
//                                                if (checkedkursi[i]) {
//                                                    stringBuilder.append(kursi[i]);
//                                                    stringBuilder.append(" ");
////                                                        dataPilih += kursi[i] + " ";
//                                                    checkedkursi[i] = false;
//                                                }
//                                            }
//                                            String dataPilih = "" + stringBuilder.toString().trim();
//                                            requestnonton(dataPilih, scanResult);
//                                        }
//                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                                    AlertDialog alert1 = builder.create();
                                    alert1.show();

//                                    kursis = jsonRESULTS.getJSONObject("user");
//                                    for (int i = 0; i < kursis.length(); ++i) {
//
//                                        JSONObject jsn = kursis.getJSONObject(String.valueOf(i));
//
//                                        String keyVal = jsn.getString("s_kursi");
//                                        Toast.makeText(mContext, keyVal, Toast.LENGTH_SHORT).show();
//                                    }
                                } else {
                                    // Jika login gagal
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                        loading.dismiss();
                    }
                });
    }

    private void requestnonton(String nonton, String hasil_scan) {
        loading = ProgressDialog.show(scan.this, null, "Harap Tunggu...", true, false);
        mApiService.nontonRequest(nonton, hasil_scan)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            loading.dismiss();
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("error").equals("false")) {
                                    String success_msg = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, success_msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Jika login gagal
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                        loading.dismiss();
                    }
                });
    }
}