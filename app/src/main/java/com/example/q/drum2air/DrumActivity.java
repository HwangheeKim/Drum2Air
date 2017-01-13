package com.example.q.drum2air;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class DrumActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    Button recordSnare, recordCrash, recordHihat, startDrum, stopDrum;
    TextView recordStatus;

    ArrayList<AccelData> accelDatas = new ArrayList<>();
    ArrayList<OrientData> orientDatas = new ArrayList<>();
    ArrayList<PreDataSet> preDataSets = new ArrayList<>();

    SensorManager sensorManager;
    Sensor acceler, orientation;

    SoundPool soundPool;
    int[] soundId = new int[3];

    int recording = -1, recordingType = -1;
    boolean swing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drum);

        recordSnare = (Button)findViewById(R.id.record_snare);
        recordCrash = (Button)findViewById(R.id.record_crash);
        recordHihat = (Button)findViewById(R.id.record_hihat);
        startDrum = (Button)findViewById(R.id.start_drum);
        stopDrum = (Button)findViewById(R.id.stop_drum);
        recordStatus = (TextView)findViewById(R.id.record_status);

        recordSnare.setOnClickListener(this);
        recordCrash.setOnClickListener(this);
        recordHihat.setOnClickListener(this);
        startDrum.setOnClickListener(this);
        stopDrum.setOnClickListener(this);

        accelDatas.add(new AccelData(0, 0, 0, 0));

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acceler = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        soundPool = new SoundPool(3, AudioManager.STREAM_ALARM, 0);
        soundId[0] = soundPool.load(this, R.raw.thud2, 1);
        soundId[1] = soundPool.load(this, R.raw.crash, 1);
        soundId[2] = soundPool.load(this, R.raw.hihat, 1);
    }

    private void initSensor() {
        sensorManager.registerListener(this, acceler, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_drum:
                startDrum.setEnabled(false);
                stopDrum.setEnabled(true);
                initSensor();
                break;
            case R.id.stop_drum:
                startDrum.setEnabled(true);
                stopDrum.setEnabled(false);
                accelDatas = new ArrayList<>();
                accelDatas.add(new AccelData(0, 0, 0, 0));
                orientDatas = new ArrayList<>();
                preDataSets = new ArrayList<>();
                sensorManager.unregisterListener(this);
                break;
            case R.id.record_snare:
                recording = 0;
                recordingType = 0;
                recordStatus.setText("Snare Recording");
                initSensor();
                break;
            case R.id.record_crash:
                recording = 0;
                recordingType = 1;
                recordStatus.setText("Crash Recording");
                initSensor();
                break;
            case R.id.record_hihat:
                recording = 0;
                recordingType = 2;
                recordStatus.setText("Hihat Recording");
                initSensor();
                break;
            default:
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        initSensor();
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            orientDatas.add(new OrientData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
            return;
        }

        double x = event.values[0];
        accelDatas.add(new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));

        if (x < -30) {
            swing = true;
        } else if ((accelDatas.get(accelDatas.size()-2).getX() < x) && swing) {
            swing = false;
            Log.d("POWER!", accelDatas.get(accelDatas.size()-1).toString());

            // If recording, set the preset
            if (recording >= 0) {
                preDataSets.add(new PreDataSet(accelDatas, orientDatas, recordingType));
                recording++;
                if(recording >= 5) {
                    recording = -1;
                    recordStatus.setText("RECORD DONE!");
                    sensorManager.unregisterListener(this);
                }
            } else {
                soundPool.play(soundId[classifierByMinDistance()], 1.0F, 1.0F, 1, 0, 1.0F);

//                // Compare the latest log to the presets
//                int minIndex = 0;
//                double minDistance = Double.MAX_VALUE;
//                for(int i=0 ; i<preDataSets.size() ; i++) {
//                    double distance = preDataSets.get(i).distance(accelDatas, orientDatas);
//                    if(distance < minDistance) {
//                        minIndex = i;
//                        minDistance = distance;
//                    }
//                }
//
//                // Find the best match
//                Log.d("Minimum Distance", "" + minDistance + " # Sound " + minIndex);
//                soundPool.play(soundId[preDataSets.get(minIndex).type], 1.0F, 1.0F, 1, 0, 1.0F);
            }
        }
    }

    public int classifierByMinDistance() {
        return 0;
    }

}