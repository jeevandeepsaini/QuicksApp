package com.example.quicksapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private String getPhoneNumber;
    private String getMessage;
    private CountryCodePicker countryCodePicker;
    private TextInputLayout phoneNumber, message;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For FullScreen: should be mentioned before setContentView
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        MaterialButton send = findViewById(R.id.send);
        message = findViewById(R.id.textMessage);
        countryCodePicker = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phoneNumber);

        checkInternetConnection();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhoneNumber = phoneNumber.getEditText().getText().toString();

                if (getPhoneNumber.isEmpty()) {
                    phoneNumber.setError("Phone Number is Invalid");
                    checkInternetConnection();
                } else {
                    checkInternetConnection();
                    phoneNumber.setError(null);
                    getMessage = message.getEditText().getText().toString();
                    countryCodePicker.registerCarrierNumberEditText(phoneNumber.getEditText());
                    getPhoneNumber = countryCodePicker.getFullNumber();

                    boolean installed = CheckWhatsappInstalledOrNot();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (installed) {
                        try {
                            intent.setPackage("com.whatsapp");
                            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + getPhoneNumber + "&text=" + URLEncoder.encode(getMessage, "UTF-8")));
                            startActivity(intent);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
                    }
                    message.clearFocus();
                    phoneNumber.clearFocus();
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return;
            }
        }  else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.ic_alert)
                    .setTitle("No Internet Connection")
                    .setMessage("Internet not available. Please check your internet connectivity and try again.")
                    .setCancelable(false)

                    .setNegativeButton("DISMISS", (dialog, id) -> dialog.cancel());
            AlertDialog alert = alertDialog.create();
            alert.show();

            Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                negativeButton.setTextColor(getResources().getColor(R.color.green));
            }
        }
    }
    private boolean CheckWhatsappInstalledOrNot() {
        PackageManager packageManager = getPackageManager();
        boolean appInstalled;
        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }catch (PackageManager.NameNotFoundException e){
            appInstalled = false;
        }
        return appInstalled;
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneNumber.clearFocus();
        message.clearFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        phoneNumber.setError(null);
        phoneNumber.clearFocus();
        message.clearFocus();
    }
}