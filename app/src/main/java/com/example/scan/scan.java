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
    public String id_pesan;
    public JSONObject kursis;
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
//        initViews();

//        recyclerView = findViewById(R.id.listkursi);
//        layoutManager = new LinearLayoutManager(scan.this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//        kirim = findViewById(R.id.button);
//        kirim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                String name = txtInputName.getText().toString();
////                txtName.setText(name);
//                customDialog.dismiss();
//            }
//        });
    }

    //    private void initViews(){
//        initCustomDialog();
//        createTable("" + scanResult);
//    }
//
//    private void initCustomDialog(){
//        customDialog = new Dialog(scan.this);
//        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        customDialog.setContentView(R.layout.custom_dialog);
//        customDialog.setCancelable(true);
//
//        recyclerView = customDialog.findViewById(R.id.listkursi);
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//        kirim = customDialog.findViewById(R.id.button);
//        kirim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                String name = txtInputName.getText().toString();
////                txtName.setText(name);
//                customDialog.dismiss();
//            }
//        });
//    }
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
//        createTable("" + scanResult);

        requestScan(scanResult);
//        AlertDialog.Builder builder = new AlertDialog.Builder(scan.this);
//        builder.setTitle("Scan Result");
//        builder.setMessage("" + id_pesan);
//        AlertDialog alert1 = builder.create();
//        alert1.show();
    }

    //    private void createTable(String type) {
//        apiInterface = UtilsApi.getAPIService();
//
//        Call<List<list_kursi>> call = apiInterface.getKursi(type);
//        call.enqueue(new Callback<List<list_kursi>>() {
//            @Override
//            public void onResponse(Call<List<list_kursi>> call, Response<List<list_kursi>> response) {
//                listkursi = response.body();
//                adapter = new ad_kursi(listkursi, scan.this, appendBooked);
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(Call<List<list_kursi>> call, Throwable t) {
//
//                Toast.makeText(scan.this, "Error\n" + t.toString(), Toast.LENGTH_LONG).show();
//            }
//        });
////        room = new Room(this,tableLayout, titleTextView, ROW, COL, filmDatabase.getSeats(showId));
////
//    }
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

                                    id_pesan = jsonRESULTS.getJSONObject("user").getString("kursi");
                                    final String[] kursi = id_pesan.split("\\s+");
                                    final boolean[] checkedkursi = new boolean[kursi.length];
                                    boolean status_checked = false;
//                                    for(int i=0; i<kursi.length; i++) {
//                                        Toast.makeText(scan.this, kursi[i], Toast.LENGTH_SHORT). show() ;
//                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(scan.this);
                                    builder.setTitle("Scan Result");
                                    for (int i = 0; i < kursi.length; i++) {
//                                        final List<String> colorsList = Arrays.asList(kursi);
                                        kursis = jsonRESULTS.getJSONObject("user");
                                        for (int j = 0; j < kursis.length(); ++j) {

                                            JSONObject jsn = kursis.getJSONObject(String.valueOf(j));

                                            String keyVal = jsn.getString("s_kursi");

                                            if (kursi.equals(keyVal)) {
                                                status_checked = true;
                                            }
                                            Toast.makeText(mContext, keyVal, Toast.LENGTH_SHORT).show();
                                        }
//                                        builder.setMessage((CharSequence) colorsList);
//                                        builder.setMultiChoiceItems(kursi,checkedkursi,new DialogInterface.OnMultiChoiceClickListener(){
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                                                String currentItem = colorsList.get(which);
//                                                Toast.makeText(getApplicationContext(),
//                                                        currentItem + " " + isChecked, Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                        builder.setItems(kursi, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                Toast.makeText(scan.this, "Position: " + which + " Value: " + kursi[which], Toast.LENGTH_LONG).show();
//                                            }
//                                        });
                                        final boolean finalStatus_checked = status_checked;
                                        builder.setMultiChoiceItems(kursi
                                                , checkedkursi, new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                                                        Toast.makeText(scan.this, "Position: " + which + " Value: " + kursi[which] + " State: " + (isChecked ? "checked" : "unchecked"), Toast.LENGTH_LONG).show();
                                                        checkedkursi[which] = finalStatus_checked;
                                                    }
                                                }
                                        );

                                    }
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
//                                                String dataPilih = "";
                                            StringBuilder stringBuilder = new StringBuilder();
                                            for (int i = 0; i < kursi.length; i++) {
                                                if (checkedkursi[i]) {
                                                    stringBuilder.append(kursi[i]);
                                                    stringBuilder.append(" ");
//                                                        dataPilih += kursi[i] + " ";
                                                    checkedkursi[i] = false;
                                                }
                                            }
                                            String dataPilih = "" + stringBuilder.toString().trim();
                                            requestnonton(dataPilih, scanResult);
//                                                Toast.makeText(scan.this, dataPilih, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                                    AlertDialog alert1 = builder.create();
                                    alert1.show();

                                    kursis = jsonRESULTS.getJSONObject("user");
                                    for (int i = 0; i < kursis.length(); ++i) {

                                        JSONObject jsn = kursis.getJSONObject(String.valueOf(i));

                                        String keyVal = jsn.getString("s_kursi");

//                                        if( kursi.equals( keyVal )  ) {
//                                            holder.itemView.setEnabled(false);
//                                            holder.kursi.setBackgroundColor(Color.parseColor("#E94818"));
//                                        }
                                        Toast.makeText(mContext, keyVal, Toast.LENGTH_SHORT).show();
                                    }
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(scan.this);
//                                    builder.setTitle("Scan Result");
//                                    builder.setMessage("" +id_pesan);
//
//                                    AlertDialog alert1 = builder.create();
//                                    alert1.show();
//                                    onResume();
//                                    Intent pindahActivity = new Intent(scan.this, coba.class);
//                                    pindahActivity.putExtra("jumlah", id_pesan);
//                                    startActivity(pindahActivity);
//                                    startActivity(new Intent(mContext, MainActivity.class)
//                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                    finish();
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

//                                    Intent pindahActivity = new Intent(scan.this, coba.class);
//                                    pindahActivity.putExtra("jumlah", id_pesan);
//                                    startActivity(pindahActivity);
//                                    startActivity(new Intent(mContext, MainActivity.class)
//                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                                    finish();
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