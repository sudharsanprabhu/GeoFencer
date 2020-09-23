package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Button close=findViewById(R.id.btnClose);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(AlarmActivity.this,GeoAlarmBackgroundService.class));
                finish();
            }
        });

    }

}
