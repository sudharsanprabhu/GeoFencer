package com.geo.geofencer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.navigation.NavigationView;

import com.notbytes.barcode_reader.BarcodeReaderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    static MapsActivity instance;

    private static final String CHANNEL_ID = "GeoFencer";

    private ActionBarDrawerToggle actionBarDrawerToggle;
    //
    private GoogleMap mMap;
    private PolygonOptions boundary = new PolygonOptions();
    private Polygon boundaryPolygon;
    private Marker clientLocationMarker;

    ArrayList<Marker> boundaryMarkers = new ArrayList<>();
    String clientName = "";
    String clientCode = "";
    String clientUsername = "";
    int isClientConnected;


    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    private GeoFence.Point[] polygon, clientPolygon;
    GeoFence.Point p;


    Button btnGeoFence;

    LatLng pos, vertexPoint, clientLatLng;
    private boolean  restoreMap = false;
    boolean isPreviousInside = false, isCurrentInside = false;
    boolean isMapReady;
    int i = 0;
    int markerCount = 0;

    ArrayList<LatLng> points = new ArrayList<>();
    double[] lat, lon;
    double clientLatitude = 0;
    double clientLongitude = 0;
    int noOfPoints = 0;
    boolean monitor = false, isClientVisible = false, monitorClient = false;

    DrawerLayout drawerLayout;
    Intent intent;

    Menu navMenu;
    MenuItem accountItem;

    Handler handler;
    Runnable runnable;

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onPause() {
        super.onPause();
        if(fusedLocationClient!=null && locationCallback!=null)
        {
            fusedLocationClient.removeLocationUpdates(locationCallback);

        }
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.commit();
        if (points.size() > 0) {

            editor.putInt("vertexCount", points.size());
            editor.putBoolean("restoreMap", true);
            for (int i = 0; i < points.size(); i++) {
                editor.putString("latitude" + i, "" + points.get(i).latitude);
                editor.putString("longitude" + i, "" + points.get(i).longitude);
            }

            editor.commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        markerCount = sharedPreferences.getInt("vertexCount", 0);
        if (markerCount > 0) {
            lat = new double[markerCount];
            lon = new double[markerCount];
        }
        for (int i = 0; i < markerCount; i++) {
            lat[i] = Double.parseDouble(sharedPreferences.getString("latitude" + i, "1000"));
            lon[i] = Double.parseDouble(sharedPreferences.getString("longitude" + i, "1000"));
            if (lat[i] != 1000.0 || lon[i] != 1000.0) {
                vertexPoint = new LatLng(lat[i], lon[i]);
                points.add(vertexPoint);
                boundary.add(vertexPoint).strokeColor(Color.RED);
            }
        }
        restoreMap = sharedPreferences.getBoolean("restoreMap", false);
        setNavigationBar();

        //To remove client location updates after removing client account

        if(getIntent().getBooleanExtra("isClientRemoved",false) && runnable!=null)
            handler.removeCallbacks(runnable);

        SharedPreferences monitorSharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.monitor",Context.MODE_PRIVATE);
        if(monitorSharedPreferences.getBoolean("monitor",false)||monitorSharedPreferences.getBoolean("monitorClient",false))
            btnGeoFence.setText(R.string.StopMonitoring);

    }


    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);


        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.commit();
        if (points.size() > 0) {

            editor.putInt("vertexCount", points.size());
            editor.putBoolean("restoreMap", true);
            for (int i = 0; i < points.size(); i++) {
                editor.putString("latitude" + i, "" + points.get(i).latitude);
                editor.putString("longitude" + i, "" + points.get(i).longitude);
            }

            editor.commit();
        }
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT))
            drawerLayout.closeDrawer(Gravity.LEFT);
        else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        instance=this;

        setNavigationBar();

        createNotificationChannel();

        btnGeoFence = findViewById(R.id.btnGeoFence);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        intent = new Intent(getApplicationContext(), BackgroundService.class);

        SharedPreferences monitorSharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.monitor",Context.MODE_PRIVATE);
        if(monitorSharedPreferences.getBoolean("monitor",false)||monitorSharedPreferences.getBoolean("monitorClient",false))
            btnGeoFence.setText(R.string.StopMonitoring);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
                return true;
        }
        else
            switch (item.getItemId()) {
                case R.id.btnDeleteAllMarkers:
                    deleteAllMarkers();
                    return true;

                case R.id.btnDrawLBoundary:
                    drawBoundary();
                    return true;

                case R.id.btnDeleteBoundary:
                    deleteBoundary();
                    return true;

                case R.id.btnNormalMap:
                    showNormalMap();
                    return true;

                case R.id.btnSatelliteMap:
                    showSatelliteMap();
                    return true;

                case R.id.btnHybridMap:
                    showHybridMap();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        //Restore Previously drawn boundary
        if (restoreMap)
            drawBoundary();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        pos = new LatLng(location.getLatitude(), location.getLongitude());

                        if (i == 0)
                        {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
                            i = 1;
                            isMapReady=true;
                        }
                    }
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng point)
            {
                monitor=false;
                monitorClient=false;

                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.monitor",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();

                editor.putBoolean("monitor",false);
                editor.putBoolean("monitorClient",false);
                editor.apply();

                btnGeoFence.setText(R.string.GeoFence);
                stopService(intent);

                points.add(point);
                markerCount++;
                MarkerOptions marker = new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title(""+markerCount);
                boundaryMarkers.add(mMap.addMarker(marker));
                boundary.add(point).strokeColor(Color.RED);
                if(boundaryPolygon!=null)
                    boundaryPolygon.remove();
                if(markerCount>=3)
                    drawBoundary();

            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }


            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    public void geoFence(View view)
    {
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.monitor",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if(markerCount<3)
        {
            Toast.makeText(this,"Please add atleast three points to draw a boundary",Toast.LENGTH_LONG).show();
            return;
        }
        if(monitor||sharedPreferences.getBoolean("monitor",false))
        {
            btnGeoFence.setText(R.string.GeoFence);
            monitor=false;
            stopService(intent);
            editor.putBoolean("monitor",false);
            editor.apply();
            return;
        }
        else if(monitorClient||sharedPreferences.getBoolean("monitorClient",false))
        {
            btnGeoFence.setText(R.string.GeoFence);
            monitorClient=false;
            editor.putBoolean("monitorClient",false);
            editor.apply();
            if(runnable!=null)
            {
                clientLocationMarker.remove();
                handler.removeCallbacks(runnable);
                runnable=null;
            }
            return;
        }

        btnGeoFence.setText(R.string.StopMonitoring);
        noOfPoints=points.size();
        polygon =new GeoFence.Point[noOfPoints];
        for(int i=0;i<noOfPoints;i++)
            polygon[i] = new GeoFence.Point(points.get(i).longitude, points.get(i).latitude);


        if(isClientVisible)
        {
            clientPolygon=new GeoFence.Point[noOfPoints];
            clientPolygon=polygon;
            monitorClient=true;
            editor.putBoolean("monitorClient",true);
            editor.apply();
            if(runnable==null)
            {
                clientLocationMarker.remove();
                getLocation(clientCode,clientUsername);
            }
        }
        else
        {
            monitor=true;
            editor.putBoolean("monitor",true);
            editor.apply();
            intent.putExtra("noOfPoints",noOfPoints);
            intent.putExtra("polygon",polygon);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent);
            else
                startService(intent);
        }
    }

    private void showNotification(String notificationMsg)
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
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND|NotificationCompat.DEFAULT_VIBRATE);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(3, builder.build());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GeoFence";
            String description = "Boundary Crossing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.shouldVibrate();
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }



    public void drawBoundary()
    {
        if(markerCount>=3)
        {
            boundaryPolygon=mMap.addPolygon(boundary);
        }
        else
        {
            Toast.makeText(this,"Please add atleast three points to draw a boundary",Toast.LENGTH_LONG).show();
        }
    }


    public void deleteAllMarkers()
    {
        markerCount=0;
        points.clear();
        boundary=null;
        boundary=new PolygonOptions();
        polygon=null;
        clientPolygon=null;
        monitorClient=false;
        monitor=false;
        btnGeoFence.setText(R.string.GeoFence);
        stopService(intent);
        for(int i=0;i<boundaryMarkers.size();i++)
            boundaryMarkers.get(i).remove();
        boundaryMarkers=null;
        boundaryMarkers=new ArrayList<>();

        deleteBoundary();
    }

    public void deleteBoundary()
    {
        if(boundaryPolygon!=null)
            boundaryPolygon.remove();
    }


    public void showNormalMap()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void showSatelliteMap()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void showHybridMap()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    public void setNavigationBar()
    {
        drawerLayout = findViewById(R.id.activity_maps);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.navigationView);

        navMenu=navigationView.getMenu();
        accountItem=navMenu.findItem(R.id.btnAccount);

        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user",Context.MODE_PRIVATE);
        if(sharedPreferences.getString("username",null)==null)
            accountItem.setTitle("Login");
        else
            accountItem.setTitle("Accounts");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.btnAccount:
                        Intent accountIntent;

                        if(accountItem.getTitle().toString().equals("Login"))
                            accountIntent = new Intent(MapsActivity.this, LoginActivity.class);
                        else
                            accountIntent=new Intent(MapsActivity.this,AccountsActivity.class);

                        startActivity(accountIntent);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.btnShare:
                        shareManager();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.btnViewSharedLocation:
                        viewSharedLocationManager();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.btnGeoAlarm:
                        final boolean[] flag = new boolean[1];
                        if(isMapReady)
                        {
                            Intent intent = new Intent(MapsActivity.this, GeoAlarmMapsActivity.class);
                            intent.putExtra("latitude", pos.latitude);
                            intent.putExtra("longitude", pos.longitude);
                            startActivity(intent);
                        }
                        else
                        {
                            final ProgressDialog progressDialog=new ProgressDialog(MapsActivity.this);
                            progressDialog.setMessage("Acquiring your location... Please wait");
                            progressDialog.setCancelable(true);
                            progressDialog.show();
                            final Handler handler=new Handler();
                            final Runnable runnable=new Runnable() {
                                @Override
                                public void run() {
                                    if(pos!=null)
                                    {
                                        progressDialog.dismiss();
                                    }
                                    handler.postDelayed(this,500);
                                }
                            };
                            handler.post(runnable);

                        }
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }


    void shareManager()
    {
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.user", Context.MODE_PRIVATE);
        Intent intent;
        if(sharedPreferences.getString("username",null)!=null)
        {
            intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        }
        else
        {
            intent = new Intent(this, LoginActivity.class);
            intent.putExtra("launchQRActivity",true);
            startActivity(intent);
        }
    }

    void viewSharedLocationManager()
    {
        mMap.clear();
        deleteAllMarkers();
        deleteBoundary();
        monitor=false;
        monitorClient=false;
        stopService(intent);
        if(runnable!=null)
        {
            handler.removeCallbacks(runnable);
            runnable=null;
        }
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.client",Context.MODE_PRIVATE);
        if(sharedPreferences.getString("clientUsername",null)!=null)
        {
            clientUsername=sharedPreferences.getString("clientUsername",null);
            getLocation("defaultClientCode",clientUsername);
        }
        else
        {
            Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(getApplicationContext(), true, false);
            startActivityForResult(launchIntent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1 && data != null)
        {
            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);
            String[] stringData = barcode.rawValue.split("&");
            if (stringData.length == 2)
            {
                clientCode = stringData[0];
                clientUsername = stringData[1];
                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.client",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("clientUsername",clientUsername);
                editor.apply();
                getLocation(clientCode, clientUsername);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Invalid Code", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void getLocation(final String clientCode, final String clientUsername)
    {
        isClientVisible=true;
        final MarkerOptions clientLocation = new MarkerOptions().position(new LatLng(0, 0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        clientLocationMarker = mMap.addMarker(clientLocation);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String url = getString(R.string.ip_getLocation);
                StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            JSONObject jObj = new JSONObject(response);
                            if(jObj.getString("code").equals(clientCode)||clientCode.equals("defaultClientCode"))
                            {
                                isClientConnected = jObj.getInt("connectionStatus");
                                if (isClientConnected==1) {
                                    clientLatitude = Double.parseDouble(jObj.getString("latitude"));
                                    clientLongitude = Double.parseDouble(jObj.getString("longitude"));
                                    clientLatLng = new LatLng(clientLatitude, clientLongitude);

                                    clientName = jObj.getString("name");

                                    clientLocationMarker.setTitle(clientName);
                                    clientLocationMarker.setSnippet("Latitude: " + clientLatitude + "\nLongitude: " + clientLongitude);
                                    clientLocationMarker.setPosition(clientLatLng);

                                    if (clientPolygon != null) {

                                        p = new GeoFence.Point(clientLongitude, clientLatitude);
                                        isCurrentInside = GeoFence.isInside(clientPolygon, noOfPoints, p);
                                        if (!isPreviousInside && isCurrentInside) {
                                            showNotification(clientName + " is inside the boundary");
                                            isPreviousInside = true;
                                        } else if (isPreviousInside && !isCurrentInside) {
                                            showNotification(clientName + " has crossed the boundary");
                                            isPreviousInside = false;
                                        }
                                    }
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),clientName+" disconnected!",Toast.LENGTH_LONG).show();
                                    clientLocationMarker.remove();
                                    handler.removeCallbacks(runnable);
                                    runnable=null;
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Incorrect Code",Toast.LENGTH_LONG).show();
                                handler.removeCallbacks(runnable);
                                runnable=null;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Posting params to register url
                        Map<String, String> params = new HashMap<>();
                        params.put("username",clientUsername);
                        return params;
                    }
                };
                // Adding request to request queue
                AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "getLocation");
                handler.postDelayed(this,5000);
            }
        };
        handler.post(runnable);
    }

}
