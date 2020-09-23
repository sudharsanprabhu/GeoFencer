package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.WriterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRActivity extends AppCompatActivity {
    ImageView qrImage;
    QRGEncoder qrgEncoder;
    TextView txtDisplay;
    Bitmap bitmap;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        qrImage = findViewById(R.id.qrView);
        txtDisplay=findViewById(R.id.txtDisplay);

        intent = new Intent(getApplicationContext(), BackgroundLocationUpdaterService.class);

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = Math.min(width, height);
        smallerDimension = smallerDimension * 3 / 4;

        String characters = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqurstuvwxyz023456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 30; i++)
            code.append(characters.charAt(random.nextInt(59)));

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.geo.geofencer.user", Context.MODE_PRIVATE);

        String url = getString(R.string.ip_sendCode);
        String username = sharedPreferences.getString("username", null);
        sendData(url, username, code.toString());

                code.append("&" + username);
                qrgEncoder = new QRGEncoder(code.toString(), null, QRGContents.Type.TEXT, smallerDimension);
                try {
                    bitmap = qrgEncoder.encodeAsBitmap();
                    qrImage.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    startForegroundService(intent);
                else
                    startService(intent);

    }

    void sendData(String url, final String username, final String code){
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtDisplay.setText(R.string.connectionFailed);
                qrImage.setImageResource(R.drawable.ic_warning);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username",username);
                params.put("id", code);
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, "sendCode");
    }
}



