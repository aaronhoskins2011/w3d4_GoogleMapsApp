package com.example.aaron.w3d4_googlemapsapp;

/**************************************************************************************
 * * ******************************************************************************* **
 * * *                                                                             * **
 * * *  2. Create geocoding/reverse geocoding using the Google Geocoding api.      * **
 * * *  3. Create geocoding/reverse geocoding using the Geocoder class             * **
 * * *  4. Animate maps to a different location on button click after entering     * **
 * * *       the coordinates.5. Use different map types6. Ask user to turn the     * **
 * * *       location on in the device if he disabled it after allowing it.        * **
 * * *                                                                             * **
 * * ******************************************************************************* **
 *************************************************************************************/

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.aaron.w3d4_googlemapsapp.model.GoogleGeolocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*********************************
 *       MAIN ACTIVITY           *
 ********************************/
public class MainActivity extends AppCompatActivity {

    //Constants
    public static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyDQQ-4FMw4JH_0CjRf6hV8nBX4KNcDyiXs";
    private static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 402;
    public static final String GEO_KEY = "AIzaSyDQQ-4FMw4JH_0CjRf6hV8nBX4KNcDyiXs";
    //Clients
    FusedLocationProviderClient fusedLocationProviderClient;
    String responseString;
    //Views
    EditText etAddress;
    EditText etLatitude;
    EditText etLongitude;


    //             //
    //  On Create  //
    //=============//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAddress = (EditText) findViewById(R.id.etAddress);
        etLatitude = (EditText) findViewById(R.id.etLatitude);
        etLongitude = (EditText) findViewById(R.id.etLongitude);

        runtimePermission();
        checkIfLocationIsOn(this);
    }

    //                       //
    //  Get Info By Address  //
    //=======================//
    public void getInfoByAddress(View view) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        getGoogleGeoLocation();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: " + e.toString());
                    }
                });
    }

    //                       //
    //  Runtime Permission   //
    //=======================//
    public void runtimePermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

      //                       //
     //  Permission Result    //
    //=======================//
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getLocation();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void getGoogleGeoLocation() {
        final OkHttpClient client = new OkHttpClient();
        String address = etAddress.getText().toString();
        final String logTag = "Get_Location_by_Addrs";
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("address", address)
                .addQueryParameter("key", GEO_KEY)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(logTag, "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                String responseString = response.body().string();
                Log.d(logTag, "onResponse: " + responseString);
                GoogleGeolocation locationFound = gson.fromJson(responseString, GoogleGeolocation.class);
                Log.d(logTag, "onResponse: " + locationFound.getResults().toString());
                double lat = locationFound.getResults().get(0).getGeometry().getLocation().getLng();
                double log = locationFound.getResults().get(0).getGeometry().getLocation().getLng();

                Log.d(logTag, "onResponse: " + "Log == " + log + " lat == " + lat);
                updateInfo("latlng", null, String.valueOf(lat), String.valueOf(log));

            }

        });


    }

    public void getInfoByLatLng(View view) {
        final OkHttpClient client = new OkHttpClient();
        String latLng = etLatitude.getText().toString() + "," + etLongitude.getText().toString();
        final String logTag = "Get_Location_by_Addrs";
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("latlng", latLng)
                .addQueryParameter("key", GEO_KEY)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(logTag, "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                String responseString = response.body().string();
                Log.d(logTag, "onResponse: " + responseString);
                GoogleGeolocation locationFound = gson.fromJson(responseString, GoogleGeolocation.class);
                Log.d(logTag, "onResponse: " + locationFound.getResults().toString());
                Log.d("TAG", "onResponse: " + locationFound.getResults().get(0).getFormattedAddress());
                updateInfo("address", locationFound.getResults().get(0).getFormattedAddress(), null, null);
            }

        });
    }

    public void updateInfo(final String whichView, final String address, final String Lat, final String lng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(whichView){
                    case "address":
                            etAddress.setText(address);
                        break;
                    case "latlng":
                            etLatitude.setText(Lat);
                            etLongitude.setText(lng);
                        break;
                }

            }

        });
    }
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void checkIfLocationIsOn(final Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}
            if(!gps_enabled && !network_enabled){
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("Please enable location settings");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(myIntent);
                        //get gps
                    }
                });
                dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                    @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }

    }
}
