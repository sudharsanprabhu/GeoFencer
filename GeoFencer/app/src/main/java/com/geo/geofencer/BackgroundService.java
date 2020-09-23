package com.geo.geofencer;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class BackgroundService extends Service {


    private static final String CHANNEL_ID = "GeoFencer" ;
    private static final String ACTION_STOP_SERVICE = "STOP";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    String notificationMsg="";
    boolean isPreviousInside=false, isCurrentInside=false;
    private GeoFence.Point[] polygon;
    int noOfPoints=0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_STOP_SERVICE.equals(intent.getAction()))
            stopSelf();
        else {

            informUserAboutService();
            noOfPoints = intent.getIntExtra("noOfPoints", 0);

            polygon = (GeoFence.Point[]) intent.getSerializableExtra("polygon");

            Toast.makeText(this, "Monitoring your location", Toast.LENGTH_LONG).show();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            GeoFence.Point p = new GeoFence.Point(location.getLongitude(), location.getLatitude());
                            isCurrentInside = GeoFence.isInside(polygon, noOfPoints, p);

                            if (!isPreviousInside && isCurrentInside) {
                                notificationMsg = "You are inside your boundary";
                                showNotification();
                                isPreviousInside = true;
                            } else if (isPreviousInside && !isCurrentInside) {
                                notificationMsg = "You have crossed your boundary";
                                showNotification();
                                isPreviousInside = false;
                            }
                        }
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Monitoring disabled", Toast.LENGTH_LONG).show();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.monitor", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("monitor",false);
        editor.apply();
        MapsActivity mapsActivity=MapsActivity.instance;
        mapsActivity.btnGeoFence=mapsActivity.findViewById(R.id.btnGeoFence);
        mapsActivity.btnGeoFence.setText(R.string.GeoFence);
        mapsActivity.monitor=false;
    }

    private void showNotification()
    {

        //intent
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("GeoFencer")
                .setContentText(notificationMsg)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

    }

    public void informUserAboutService()
    {
        Intent stopMonitoringIntent=new Intent(this, BackgroundService.class);
        stopMonitoringIntent.setAction(ACTION_STOP_SERVICE);

        PendingIntent stopMonitoringPendingIntent=PendingIntent.getService(this,0,stopMonitoringIntent,PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
            Notification notification=builder.setOngoing(true)
                    .setContentTitle("GeoFencer")
                    .setContentText("Background Location Access")
                    .setSmallIcon(R.drawable.icon)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory((NotificationCompat.CATEGORY_ALARM))
                    .addAction(R.drawable.icon,"Stop Monitoring",stopMonitoringPendingIntent)
                    .build();
            startForeground(2,notification);

    }

}
