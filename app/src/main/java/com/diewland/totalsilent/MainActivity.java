package com.diewland.totalsilent;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private Switch toggle;
    private TextView out;
    private HashMap<String, Integer> sound_types;
    private AudioManager audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize starter values
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        toggle = (Switch)findViewById(R.id.switch1);
        out = (TextView)findViewById(R.id.out);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // collect sound types
        sound_types = new HashMap<String, Integer>();
        sound_types.put("ALARM", AudioManager.STREAM_ALARM);
        sound_types.put("DTMF", AudioManager.STREAM_DTMF);
        sound_types.put("MUSIC", AudioManager.STREAM_MUSIC);
        sound_types.put("NOTIFICATION", AudioManager.STREAM_NOTIFICATION);
        sound_types.put("RING", AudioManager.STREAM_RING);
        sound_types.put("SYSTEM", AudioManager.STREAM_SYSTEM);
        sound_types.put("VOICE_CALL", AudioManager.STREAM_VOICE_CALL);

        // bind switch button
        // TODO keep toggle stage when click, onResume
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    turnOffSound();
                    updateInfo();
                }
                else {
                    turnOnSound();
                    updateInfo();
                }
            }
        });

        // update current values
        updateInfo();
        keepCurrentValues();
    }

    private void updateInfo(){
        out.setText("");
        for(Map.Entry<String, Integer> entry : sound_types.entrySet()){
            String k = entry.getKey();
            Integer v = audio.getStreamVolume(entry.getValue());
            Integer x = audio.getStreamMaxVolume(entry.getValue());
            String line = String.format("%02d-%02d | %s\n", v, x, k);
            out.append(line);
        }
    }

    private void keepCurrentValues(){
        //
        // TODO check silent ?
        //
        SharedPreferences.Editor editor = sharedPref.edit();
        for(Map.Entry<String, Integer> entry : sound_types.entrySet()) {
            editor.putInt(entry.getKey(), entry.getValue());
        }
        editor.commit();
    }

    private void turnOnSound(){
         for(Map.Entry<String, Integer> entry : sound_types.entrySet()) {
            int cur_v = sharedPref.getInt(entry.getKey(),5);
            audio.setStreamVolume(entry.getValue(), cur_v, 0);
        }
    }

    private void turnOffSound(){
         for(Map.Entry<String, Integer> entry : sound_types.entrySet()) {
            audio.setStreamVolume(entry.getValue(), 0, 0);
        }
    }

}
