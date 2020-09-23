package com.geo.geofencer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;


public class GeoAlarmBackgroundService extends Service {


    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private  MediaPlayer mediaPlayer=new MediaPlayer();
    private Vibrator v;
    PendingIntent stopAlarmPendingIntent;
    Handler handler=new Handler();
    Runnable runnable;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if("StopAlarm".equals(intent.getAction()))
        {
            stopSelf();
            return START_STICKY;
        }

        informUserAboutService();


        double lat = intent.getDoubleExtra("latitude", 0);
        double lon = intent.getDoubleExtra("longitude", 0);

        final float radius = intent.getFloatExtra("radius", 0);
        final LatLng point=new LatLng(lat,lon);

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
               if(locationResult==null)
                   return;

               for(Location location:locationResult.getLocations())
               {
                   Log.d("Location",""+location.getLatitude()+" , "+location.getLongitude());
                   if(SphericalUtil.computeDistanceBetween(point,new LatLng(location.getLatitude(),location.getLongitude()))<=radius)
                   {
                       fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                       //Sound
                       Uri uri;
                       boolean vibrate;
                       SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
                       if(sharedPreferences.getString("soundName",null)!=null)
                       {
                           uri= Uri.parse(sharedPreferences.getString("uri",""));
                           vibrate=sharedPreferences.getBoolean("isVibrationEnabled",false);

                           mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                           try {
                               mediaPlayer.setDataSource(getApplicationContext(),uri);
                               mediaPlayer.prepare();
                               mediaPlayer.start();
                               mediaPlayer.setLooping(true);
                           } catch (IOException e) {
                               e.printStackTrace();
                           }

                           if(vibrate)
                           {
                               v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                               if (v != null)
                               {
                                   if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                                   {

                                       runnable=new Runnable()
                                       {
                                           @Override
                                           public void run() {
                                               v.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
                                               handler.postDelayed(this,2000);
                                           }
                                       };
                                       handler.post(runnable);
                                   }
                                   else
                                   {
                                       v.vibrate(new long[]{0, 1000, 100}, 0);
                                   }
                               }
                           }
                       }


                       Intent intent=new Intent(getApplicationContext(),AlarmActivity.class);
                       PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                       NotificationCompat.Builder builder=new NotificationCompat.Builder(getApplicationContext(),"GeoAlarm")
                               .setContentTitle("GeoAlarm")
                               .setContentText("You have entered your boundary region")
                               .setSmallIcon(R.drawable.icon)
                               .setAutoCancel(true)
                               .setFullScreenIntent(pendingIntent,true)
                               .setPriority(NotificationCompat.PRIORITY_MIN)
                               .setCategory(NotificationCompat.CATEGORY_ALARM)
                               .addAction(R.drawable.icon,"Stop Alarm",stopAlarmPendingIntent)
                               .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

                       NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(getApplicationContext());
                       notificationManagerCompat.notify(1000,builder.build());

                       break;
                   }
               }

            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        mediaPlayer.release();
        mediaPlayer=null;
        if(runnable!=null)
            handler.removeCallbacks(runnable);
        if(v!=null)
          v.cancel();

        GeoAlarmMapsActivity geoAlarmMapsActivity=GeoAlarmMapsActivity.instance;
        geoAlarmMapsActivity.geoAlarmButton.setText(R.string.set_alarm);
        geoAlarmMapsActivity.isAlarmEnabled=false;
    }

    private void informUserAboutService()
    {
        Intent stopAlarmIntent=new Intent(this,GeoAlarmBackgroundService.class);
        stopAlarmIntent.setAction("StopAlarm");

        stopAlarmPendingIntent = PendingIntent.getService(this,0,stopAlarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"GeoAlarm");
        Notification notification=builder.setOngoing(true)
                .setContentTitle("GeoAlarm")
                .setContentText("Background location access")
                .setSmallIcon(R.drawable.icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_SOUND|NotificationCompat.DEFAULT_VIBRATE)
                .addAction(R.drawable.icon,"Cancel Alarm",stopAlarmPendingIntent)
                .build();
        startForeground(1000,notification);

    }
}
