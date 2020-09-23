package com.geo.geofencer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class GeoAlarmMapsActivity extends AppCompatActivity implements OnMapReadyCallback {


     static GeoAlarmMapsActivity instance;
    Button geoAlarmButton;

    MarkerOptions markerOptions =new MarkerOptions().position(new LatLng(0,0)).visible(false);
    CircleOptions circleOptions=new CircleOptions().center(new LatLng(0,0)).radius(0).strokeColor(Color.RED);

    private double initLatitude,initLongitude, lat=1000,lon=1000;
    private float radius=1000000000;

    private SharedPreferences sharedPreferences;

    public boolean isAlarmEnabled;


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.alarm_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId()==R.id.btnSettings)
        {
            startActivity(new Intent(GeoAlarmMapsActivity.this,SettingsActivity.class));
        }
       return true;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        sharedPreferences = getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
        radius=sharedPreferences.getFloat("bufferRadius",1000000000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_alarm_maps);

        createNotificationChannel();
        checkFullscreenPermission();

        instance =this;


        sharedPreferences = getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
        radius=sharedPreferences.getFloat("bufferRadius",1000000000);


        geoAlarmButton = findViewById(R.id.btnGeoAlarm);
        geoAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isAlarmEnabled)
                {
                    geoAlarmButton.setText(R.string.set_alarm);
                    isAlarmEnabled=false;
                    stopService(new Intent(GeoAlarmMapsActivity.this,GeoAlarmBackgroundService.class));
                    return;
                }


                if(lat!=1000&&lon!=1000&&radius!=1000000000)
                {
                    geoAlarmButton.setText(R.string.cancel_alarm);
                    isAlarmEnabled=true;
                    Intent intent=new Intent(getApplicationContext(),GeoAlarmBackgroundService.class);
                    intent.putExtra("latitude",lat);
                    intent.putExtra("longitude",lon);
                    intent.putExtra("radius",radius);

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                        startForegroundService(intent);
                    else
                        startService(intent);

                }
                else
                {
                    Toast.makeText(GeoAlarmMapsActivity.this,"Please specify a point and a radius",Toast.LENGTH_LONG).show();
                }
            }
        });


        Intent intent=getIntent();
        initLatitude=intent.getDoubleExtra("latitude",0);
        initLongitude=intent.getDoubleExtra("longitude",0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initLatitude,initLongitude),20));
        final Marker marker= googleMap.addMarker(markerOptions);
        final Circle circle= googleMap.addCircle(circleOptions);
        //OnMapCLick
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(radius!=1000000000)
                {
                    lat = point.latitude;
                    lon = point.longitude;
                    markerOptions.position(point);
                    marker.setPosition(point);
                    marker.setVisible(true);
                    marker.setTitle("Radius: "+radius);
                    circle.setCenter(point);
                    circle.setRadius(radius);
                }
                else
                {
                    startActivity(new Intent(GeoAlarmMapsActivity.this,SettingsActivity.class));
                }
            }
        });

    }



    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel("GeoAlarm","GeoAlarm",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("GeoAlarm");
            notificationChannel.enableVibration(true);
            notificationChannel.shouldVibrate();

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            if(notificationManager!=null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void checkFullscreenPermission()
    {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q)
        {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.USE_FULL_SCREEN_INTENT)!=PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.USE_FULL_SCREEN_INTENT},10);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==10)
        {
            if(grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED)
                showAlertMessage();
        }
    }

    private void showAlertMessage()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Permission Required");
        builder.setMessage("Please grant permission to show alert message");
        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkFullscreenPermission();
            }
        });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
}
