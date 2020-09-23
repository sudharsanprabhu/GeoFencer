package com.geo.geofencer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

public class BackgroundLocationUpdaterService extends Service {
    private static final String CHANNEL_ID = "GeoFencer" ;
    private static final String ACTION_STOP_SERVICE = "STOP";

    String username;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction()))
            stopSelf();
        else
        {
            informUserAboutService();


            SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user",MODE_PRIVATE);
            username = sharedPreferences.getString("username",null);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(1000);


            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (final Location location : locationResult.getLocations()) {
                        if (location != null)
                        {
                            sendLocation(username,location);
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
        sendConnectionStatus(username);
        Intent intent=new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }


    private void informUserAboutService()
    {
        Intent stopMonitoringIntent=new Intent(this, BackgroundLocationUpdaterService.class);
        stopMonitoringIntent.setAction(ACTION_STOP_SERVICE);

        PendingIntent stopMonitoringPendingIntent=PendingIntent.getService(this,0,stopMonitoringIntent,PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
            Notification notification=builder.setOngoing(true)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("GeoFencer")
                    .setContentText("Background location access")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .addAction(R.drawable.icon,"Stop Monitoring",stopMonitoringPendingIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE|Notification.DEFAULT_SOUND)
                    .build();

        startForeground(5, notification);
    }

    private void sendLocation(final String username, final Location location)
    {
        String url=getString(R.string.ip_setLocation);
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username",username);
                params.put("latitude",""+location.getLatitude());
                params.put("longitude",""+location.getLongitude());
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "sendCode");
    }

    private void sendConnectionStatus(final String username)
    {
        String url=getString(R.string.ip_sendConnectionStatus);
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username",username);
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "sendConnectionStatus");
    }

}
