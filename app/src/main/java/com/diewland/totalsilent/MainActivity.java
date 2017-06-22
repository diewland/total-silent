package com.diewland.totalsilent;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private TextView out;
    private FloatingActionButton fab;
    private HashMap<String, Integer> sound_types;
    private AudioManager audio;
    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSound();
                updateInfo();
            }
        });

        // initialize starter values
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        out = (TextView)findViewById(R.id.out);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // collect sound types
        sound_types = new HashMap<String, Integer>();
        sound_types.put("ALARM", AudioManager.STREAM_ALARM);
        sound_types.put("DTMF", AudioManager.STREAM_DTMF);
        sound_types.put("MUSIC", AudioManager.STREAM_MUSIC);
        sound_types.put("NOTIFICATION", AudioManager.STREAM_NOTIFICATION);
        sound_types.put("RING", AudioManager.STREAM_RING);
        sound_types.put("SYSTEM", AudioManager.STREAM_SYSTEM);
        sound_types.put("VOICE_CALL", AudioManager.STREAM_VOICE_CALL);

        // update current values
        keepCurrentValues();
        updateInfo();
    }

    private void keepCurrentValues(){
        if(!isSilent()){
            int ring = audio.getStreamVolume(AudioManager.STREAM_RING);
            int music = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int notif = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("PREV_RING", ring);
            editor.putInt("PREV_MUSIC", music);
            editor.putInt("PREV_NOTIF", notif);
            editor.commit();
        }
    }

    private boolean isSilent(){
        int ring = audio.getStreamVolume(AudioManager.STREAM_RING);
        int music = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int notif = audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        return (ring == 0)&&(music == 0)&&(notif == 0);
    }

    private void toggleSound(){
        int cur_ring = 0;
        int cur_music = 0;
        int cur_notif = 0;

        if(isSilent()){ // silent -> sound
            cur_ring = sharedPref.getInt("PREV_RING", 7);
            cur_music = sharedPref.getInt("PREV_MUSIC", 7);
            cur_notif = sharedPref.getInt("PREV_NOTIF", 7);
        }
        else { // sound -> silent
            keepCurrentValues();
            vib.vibrate(400);
        }
        audio.setStreamVolume(AudioManager.STREAM_RING, cur_ring, 0);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, cur_music, 0);
        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, cur_notif, 0);
    }

    private void updateInfo(){
        out.setText("##### CURRENT #####\n\n");
        for(Map.Entry<String, Integer> entry : sound_types.entrySet()){
            String k = entry.getKey();
            Integer v = audio.getStreamVolume(entry.getValue());
            out.append(k +": "+ v +"\n");
        }
        out.append("\n##### PREVIOUS #####\n\n");
        int cur_ring = sharedPref.getInt("PREV_RING", 7);
        int cur_music = sharedPref.getInt("PREV_MUSIC", 7);
        int cur_notif = sharedPref.getInt("PREV_NOTIF", 7);
        out.append("RING: "+ cur_ring +"\n");
        out.append("MUSIC: "+ cur_music +"\n");
        out.append("NOTIFICATION: "+ cur_notif +"\n");

        // update icon
        if(isSilent()){
            // TODO set vibrate icon
        }
        else {
            // TODO set sound icon
        }
    }

    @Override
    protected void onResume() {
        keepCurrentValues();
        updateInfo();
        super.onResume();
    }
}
