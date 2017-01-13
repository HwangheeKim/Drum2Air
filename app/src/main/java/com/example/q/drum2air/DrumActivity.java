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
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        soundId[0] = soundPool.load(this, R.raw.snare, 1);
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
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            orientDatas.add(0, new OrientData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
            return;
        }

        double x = event.values[0];
        accelDatas.add(0, new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));

        if (x < -30) {
            swing = true;
        } else if (swing && (accelDatas.get(1).getX() < x)) {
            swing = false;
            Log.d("POWER!", accelDatas.get(0).toString());

            // If recording, set the preset
            if (recording >= 0) {
                preDataSets.add(new PreDataSet(accelDatas, orientDatas, recordingType));
                recording++;
                if (recording >= 5) {
                    recording = -1;
                    recordStatus.setText("RECORD DONE!");
                    sensorManager.unregisterListener(this);
                }
            } else {
//                soundPool.play(soundId[classifierBykNN(7)], 1.0F, 1.0F, 1, 0, 1.0F);
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

    private int classifierBykNN(final int k) {
        ArrayList<Pair<Double, Integer>> distances = new ArrayList<>();

        for(int i=0 ; i<preDataSets.size() ; i++) {
            distances.add(Pair.create(preDataSets.get(i).distance(accelDatas, orientDatas),
                                      preDataSets.get(i).type));
        }

        Collections.sort(distances, new Comparator<Pair<Double, Integer>>() {
            @Override
            public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                return (int)(o1.first-o2.first);
            }
        });

        int[] count = {0, 0, 0};
        count[distances.get(0).second]++;
        for(int i=0 ; i<k ; i++) {
            count[distances.get(i).second]++;
        }

        int maxCount = 0, maxIndex = 0;
        for(int i=0 ; i<count.length ; i++) {
            if(count[i] > maxCount) {
                maxIndex = i;
                maxCount = count[i];
            }
        }

        return maxIndex;
    }

    public int classifierByMinDistance() {
        ArrayList<Double> diff = new ArrayList<>();
        diff.add((double) 0);
        diff.add((double) 0);
        diff.add((double) 0);
        int type;
        for (int i = 0; i < preDataSets.size(); i++) {
            type = preDataSets.get(i).type;
            diff.set(type, diff.get(type) + preDataSets.get(i).distance(accelDatas, orientDatas));
        }
        Log.d("DIFF", "0 : " + diff.get(0) + ", 1 : " + diff.get(1) + ", 2 : " + diff.get(2));
        return diff.indexOf(Collections.min(diff));
    }

}