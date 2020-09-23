package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends AppCompatActivity {

    float bufferRadius;
    TextView currentSound;


    @SuppressLint("SetTextI18n")
    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("soundName",null)!=null)
        {
            currentSound.setText(getString(R.string.selectedSounds) +" "+sharedPreferences.getString("soundName",""));
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveButton=findViewById(R.id.btnSave);
        Button pickButton=findViewById(R.id.btnPick);

        currentSound = findViewById(R.id.txtCurrentSound);
        final EditText txtBufferRadius=findViewById(R.id.txtRadius);
        final Switch vibration=findViewById(R.id.vibration);


        final SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor=sharedPreferences.edit();


        if(sharedPreferences.getString("soundName",null)!=null)
        {
            currentSound.setText(getString(R.string.selectedSounds) +" "+sharedPreferences.getString("soundName",""));
        }

        if(sharedPreferences.getFloat("bufferRadius",-1)!=-1)
        {
            txtBufferRadius.setText(""+sharedPreferences.getFloat("bufferRadius",0));
        }


        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,ListSoundsActivity.class));
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(txtBufferRadius.getText()))
                    Toast.makeText(getApplicationContext(),"Please specify a buffer radius value",Toast.LENGTH_LONG).show();
                else
                {
                    bufferRadius = Float.parseFloat(txtBufferRadius.getText().toString());
                    if(bufferRadius<0)
                    {
                        Toast.makeText(getApplicationContext(),"Buffer radius should not be negative",Toast.LENGTH_LONG).show();
                    }
                    else if(bufferRadius<=30)
                       Toast.makeText(getApplicationContext(),"Buffer radius should be greater than 30m due to GPS accuracy limitations",Toast.LENGTH_LONG).show();
                    else if(bufferRadius==0)
                        Toast.makeText(getApplicationContext(),"Buffer radius should not be zero",Toast.LENGTH_LONG).show();
                   else if(bufferRadius>20037500)
                    {
                        Toast.makeText(getApplicationContext(),"Buffer radius should not be greater than half of circumference of Earth",Toast.LENGTH_LONG).show();
                    }
                   else
                    {
                        editor.putFloat("bufferRadius", bufferRadius);
                        editor.putBoolean("isVibrationEnabled", vibration.isChecked());
                        editor.apply();
                        finish();
                    }
                }
            }
        });

    }
}
