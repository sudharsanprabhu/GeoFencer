package com.geo.geofencer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements LocationListener {
     int locationAccess=1,backgroundLocationAccess=2;
     int flag=0;

     LocationManager locationManager;
     AlertDialog requestLocationAccessAlertDialog;
     AlertDialog requestPermissionAlertDialog;



     @Override
     public void onDestroy()
     {
         super.onDestroy();
         if(requestPermissionAlertDialog!=null && requestPermissionAlertDialog.isShowing())
             requestPermissionAlertDialog.dismiss();

         if(requestLocationAccessAlertDialog!=null && requestLocationAccessAlertDialog.isShowing())
             requestLocationAccessAlertDialog.dismiss();

         if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q && requestPermissionAlertDialog!=null && requestPermissionAlertDialog.isShowing())
               requestPermissionAlertDialog.dismiss();

         locationManager.removeUpdates(this);

     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        if (checkPermission())
            {
                if (locationManager != null)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        requestLocation();
                    }
                }
            }
        }


    public boolean checkPermission()
    {
        //For Android Q
        // check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                return true;

            else {
                showAlertMessage("Please allow location access",backgroundLocationAccess);
                return false;
            }
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else
            {
            showAlertMessage("Please allow location access",locationAccess);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == locationAccess)
        {
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
              finish();
            else
            {
                //Restart
                finish();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
            }
        }
        else if (requestCode == backgroundLocationAccess) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    flag = 1;
                    break;
                }
            }

            if (flag == 1)
                finish();
            else
            {
                //Restart
                finish();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
            }
        }

    }

    private void showAlertMessage(String message, final int code)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if(code==backgroundLocationAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION}, code);
                else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, code);
            }
        });

        alertDialogBuilder.setNegativeButton("Don't Allow",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setIcon(R.drawable.icon);

        requestPermissionAlertDialog = alertDialogBuilder.create();
        requestPermissionAlertDialog.show();
        Log.d("Alert","Showing");
    }


    private void requestLocation()
    {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please enable GPS location");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                }
            });

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            requestLocationAccessAlertDialog = alertDialogBuilder.create();
            requestLocationAccessAlertDialog.show();
            Log.d("Request","Showing");
        }




    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        if(requestLocationAccessAlertDialog !=null && requestLocationAccessAlertDialog.isShowing())
         requestLocationAccessAlertDialog.dismiss();
      if(checkPermission()) {
          finish();
          startActivity(new Intent(getApplicationContext(), MapsActivity.class));
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
       if(!requestLocationAccessAlertDialog.isShowing())
             requestLocation();
    }

}
