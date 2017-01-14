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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Drum2Activity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    Button recordSnare, recordCrash, recordHihat, startDrum, stopDrum;
    Button soundSnare, soundMidtom, soundTom;
    TextView recordStatus;

    ArrayList<AccelData> accelDatas = new ArrayList<>();
    ArrayList<OrientData> orientDatas = new ArrayList<>();
    ArrayList<PreDataSet> preDataSets = new ArrayList<>();

    SensorManager sensorManager;
    Sensor acceler, orientation, gyroscope;

    SoundPool soundPool;
    int[] soundId = new int[5];

    int recording = -1, recordingType = -1;
    boolean swing = false;

    double currentX = 0, currentY = 0, currentZ = 0, lastX = 0, lastY = 0, lastZ = 0;
    int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drum);

        recordSnare = (Button) findViewById(R.id.record_snare);
        recordCrash = (Button) findViewById(R.id.record_crash);
        recordHihat = (Button) findViewById(R.id.record_hihat);
        startDrum = (Button) findViewById(R.id.start_drum);
        stopDrum = (Button) findViewById(R.id.stop_drum);
        recordStatus = (TextView) findViewById(R.id.record_status);
        soundSnare = (Button) findViewById(R.id.sound_snare);
        soundMidtom = (Button) findViewById(R.id.sound_midtom);
        soundTom = (Button) findViewById(R.id.sound_tom);

        recordSnare.setOnClickListener(this);
        recordCrash.setOnClickListener(this);
        recordHihat.setOnClickListener(this);
        startDrum.setOnClickListener(this);
        stopDrum.setOnClickListener(this);
        soundSnare.setOnClickListener(this);
        soundMidtom.setOnClickListener(this);
        soundTom.setOnClickListener(this);

        accelDatas.add(new AccelData(0, 0, 0, 0));

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acceler = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundId[0] = soundPool.load(this, R.raw.snare, 1);
        soundId[1] = soundPool.load(this, R.raw.crash, 1);
        soundId[2] = soundPool.load(this, R.raw.hihat, 1);
        soundId[3] = soundPool.load(this, R.raw.midtom, 1);
        soundId[4] = soundPool.load(this, R.raw.tom, 1);
    }

    private void initSensor() {
        sensorManager.registerListener(this, acceler, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
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
                currentX = 0;
                currentY = 0;
                currentZ = 0;
                state = 0;
                lastX =0 ;
                lastY = 0;
                lastZ =0;
                first = true;
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
            case R.id.sound_snare:
                Log.d("SNARE", "SNARE!!!!!!!!!!!!!!!!!!!!!!!!!");
                soundPool.play(soundId[0], 1.0F, 1.0F, 1, 0, 1.0F);
                break;
            case R.id.sound_midtom:
                Log.d("MIDTOM", "MIDTOM!!!!!!!!!!!!!!!!!!!!!!!!!");
                soundPool.play(soundId[3], 1.0F, 1.0F, 1, 0, 1.0F);
                break;
            case R.id.sound_tom:
                Log.d("TOM", "TOM!!!!!!!!!!!!!!!!!!!!!!!!!");
                soundPool.play(soundId[4], 1.0F, 1.0F, 1, 0, 1.0F);
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

    double prevZ=0;
    boolean first = true;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_GYROSCOPE) { return; }

        currentX += event.values[0];
        currentY += event.values[1];
        currentZ += event.values[2];

        if (event.values[2] < -4) {
            swing = true;
        } else if (swing && prevZ < event.values[2]) {
            swing = false;
            Log.d("POWER!", accelDatas.get(0).toString());
            if(!first) {
                state = newState();
            }
            soundPool.play(soundId[state], 1.0F, 1.0F, 1, 0, 1.0F);
            Log.d("Current/Last Coordinate", String.format("%f %f %f / %f %f %f", currentX, currentY, currentZ, lastX, lastY, lastZ) );
            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            first = false;
        }

        prevZ = event.values[2];
    }

    private int newState() {
        int hMargin = 80;
        switch (state) {
            case 0:
                if (lastZ + 80 < currentZ) {
                    if(lastY - hMargin * 2 > currentY) return 1;
                    return 2;
                }
                if (lastY - hMargin * 2 > currentY) return 4;
                if (lastY - hMargin > currentY) return 3;
                return 0;

            case 1:
                if (lastZ - 80 > currentZ) {
                    if(lastY + hMargin * 2 < currentY) return 0;
                    if(lastY + hMargin < currentY) return 3;
                    return 4;
                }
                if (lastY + hMargin * 2 < currentY) return 2;
                return 1;

            case 2:
                if (lastZ - 80 > currentZ) {
                    if (lastY - hMargin * 2 > currentY) return 4;
                    if (lastY - hMargin > currentY) return 3;
                    return 0;
                }
                if (lastY - hMargin * 2 > currentY) return 1;
                return 2;

            case 3:
                if (lastZ + 80 < currentZ) {
                    if(lastY < currentY) return 2;
                    return 1;
                }
                if (lastY + hMargin < currentY) return 0;
                if (lastY - hMargin > currentY) return 4;
                return 3;

            case 4:
                if (lastZ + 80 < currentZ) {
                    if(lastY + hMargin * 2 < currentY) return 2;
                    return 1;
                }
                if (lastY + hMargin * 2 < currentY) return 0;
                if (lastY + hMargin < currentY) return 3;
                return 4;

            default:
                return state;
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

