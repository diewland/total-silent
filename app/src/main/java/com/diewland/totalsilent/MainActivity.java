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

    private String STATE_SILENT = "STATE_SILENT";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
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
        editor = sharedPref.edit();
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

        // sync current sound info
        sync();

        // bind switch button
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                toggleSound(isChecked);
                setSilentState(isChecked);
                updateInfo();
            }
        });
    }

    // ##### SYNC FUNCTION #####

    private void sync(){
        if(!getSilentState()){
            // if not silent, update latest sound info
            for(Map.Entry<String, Integer> entry : sound_types.entrySet()) {
                Integer v = audio.getStreamVolume(entry.getValue());
                editor.putInt(entry.getKey(), v);
            }
            editor.commit();
        }
        else {
            // if silent, update switch state
            toggleLabel(true);
            toggle.setChecked(true);
        }
        // display info
        updateInfo();
    }

    // ##### STATE FUNCTION #####

    private boolean getSilentState(){
        return sharedPref.getBoolean(STATE_SILENT, false);
    }
    private void setSilentState(boolean newState){
        editor.putBoolean(STATE_SILENT, newState);
        editor.commit();
    }

    // ##### TOGGLE FUNCTION #####

    private void toggleLabel(boolean silentFlag){
        String label = silentFlag ? "Sound OFF" : "Sound ON";
        toggle.setText(label);
    }

    private void toggleSound(boolean silentFlag){
        toggleLabel(silentFlag);
        for(Map.Entry<String, Integer> entry : sound_types.entrySet()){
            if(silentFlag){
                audio.setStreamVolume(entry.getValue(), 0, 0);
            }
            else {
                int v = sharedPref.getInt(entry.getKey(),0);
                audio.setStreamVolume(entry.getValue(), v, 0);
            }
        }
    }

    // ##### DISPLAY FUNCTION #####

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

    // ##### HANDLE EVENT #####

    @Override
    protected void onResume() {
        super.onResume();
        sync();
    }
}
