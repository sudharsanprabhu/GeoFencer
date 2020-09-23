package com.geo.geofencer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

public class ListSoundsActivity extends AppCompatActivity {

     MediaPlayer mediaPlayer;
     String[] title;
     Uri[] alarms;
     int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sounds);




        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

        //Get Ringtone
        RingtoneManager ringtoneMgr = new RingtoneManager(getApplicationContext());
        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        int alarmsCount = alarmsCursor.getCount();
        title=new String[alarmsCount];
        alarms=new Uri[alarmsCount];

        if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
            return;
        }

        while(!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
            int currentPosition = alarmsCursor.getPosition();
            alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
            title[currentPosition]=alarmsCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
        }
        alarmsCursor.close();


        ArrayAdapter adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, title);

        ListView listView=findViewById(R.id.soundList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                index=position;

                    mediaPlayer.release();
                    mediaPlayer=null;
                    mediaPlayer=new MediaPlayer();


                try
                {
                    mediaPlayer.setDataSource(ListSoundsActivity.this,alarms[position]);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onBackPressed()
    {
        mediaPlayer.release();
        mediaPlayer=null;

        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("com.geo.geofencer.settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString("uri",""+alarms[index]);
        editor.putString("soundName",title[index]);
        editor.commit();

        finish();
    }
}
