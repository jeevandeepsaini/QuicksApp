package com.quicksapp.android;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class MessageActivity extends AppCompatActivity {

    private String defaultApp;
    private String phone;
    private String textMessage;
    private boolean connected = false;
    private CountryCodePicker countryCodePicker;
    private TextInputLayout phoneNumber, message;
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        message = findViewById(R.id.typeMessage);
        countryCodePicker = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phoneNumber);
        lottieAnimationView = findViewById(R.id.send);

        defaultApp = "WhatsApp";

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.ic_alert)
                    .setTitle("No Internet Connection")
                    .setMessage("You are offline please check your internet connection and try again.")
                    .setCancelable(false)
                    .setNegativeButton("DISMISS", (dialog, id) -> dialog.cancel());
            AlertDialog alert = alertDialog.create();
            alert.show();
        }

        lottieAnimationView.setOnClickListener(v -> {
            if (!CheckPhoneNumber()) {
                Toast.makeText(MessageActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
            } else {
                phone = phoneNumber.getEditText().getText().toString();
                textMessage = message.getEditText().getText().toString();

                countryCodePicker.registerCarrierNumberEditText(phoneNumber.getEditText());
                phone = countryCodePicker.getFullNumber();

                boolean installed = CheckWhatsappInstalledOrNot("com.whatsapp");
                boolean installedWB = CheckWhatsappInstalledOrNot("com.whatsapp.w4b");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (installed || installedWB) {
                    if (defaultApp.equalsIgnoreCase("WhatsApp")) {
                        intent.setPackage("com.whatsapp");
                        //Log.e("message", "WhatsApp");
                    } else if (defaultApp.equalsIgnoreCase("BusinessWhatsApp")) {
                        intent.setPackage("com.whatsapp.w4b");
                        //Log.e("message", "Business WhatsApp");
                    }
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone="+phone+"&text="+textMessage));
                } else {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
                }
                startActivity(intent);
                message.clearFocus();
                phoneNumber.clearFocus();
            }
        });
    }

    private boolean CheckWhatsappInstalledOrNot(String uri) {
        PackageManager packageManager = getPackageManager();
        boolean appInstalled;
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }catch (PackageManager.NameNotFoundException e){
            appInstalled = false;
        }
        return appInstalled;
    }

    private boolean CheckPhoneNumber () {
        if (phoneNumber.getEditText().getText().toString().isEmpty()) {
            phoneNumber.setError("Invalid Phone Number");
            return false;
        } else {
            phoneNumber.setError(null);
            return true;
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}