package com.example.q.drum2air;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DrumActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener{

    Button button_hihat, button_snare, button_midtom, button_crash, button_floortom, button_bass, button_hand;

    SensorManager sensorManager;
    Sensor sensorGyroscope;

    SoundPool soundPool;
    int[] soundId = new int[6];

    double currentX = 0, currentY = 0, currentZ = 0, lastX = 0, lastY = 0, lastZ = 0, prevZ = 0;
    int state = 3;
    boolean first = true;
    boolean swing = false;
    boolean rightHanded = true;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbManager usbManager;
    UsbDeviceConnection connection;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbSerialInterface.UsbReadCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drum);

        button_hihat = (Button) findViewById(R.id.button_hihat);
        button_snare = (Button) findViewById(R.id.button_snare);
        button_midtom = (Button) findViewById(R.id.button_midtom);
        button_crash = (Button) findViewById(R.id.button_crash);
        button_floortom = (Button) findViewById(R.id.button_floortom);
        button_bass = (Button) findViewById(R.id.button_bass);
        button_hand = (Button) findViewById(R.id.button_hand);

        button_hihat.setOnClickListener(this);
        button_snare.setOnClickListener(this);
        button_midtom.setOnClickListener(this);
        button_crash.setOnClickListener(this);
        button_floortom.setOnClickListener(this);
        button_bass.setOnClickListener(this);
        button_hand.setOnClickListener(this);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        soundId[0] = soundPool.load(this, R.raw.snare, 1);
        soundId[1] = soundPool.load(this, R.raw.crash, 1);
        soundId[2] = soundPool.load(this, R.raw.hihat, 1);
        soundId[3] = soundPool.load(this, R.raw.midtom, 1);
        soundId[4] = soundPool.load(this, R.raw.floortom, 1);
        soundId[5] = soundPool.load(this, R.raw.bass, 1);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mCallback = new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] bytes) {
                if (bytes != null && bytes.length > 0 && bytes[0] == 'T') {
                    soundPool.play(soundId[5], 1.0F, 1.0F, 1, 0, 1.0F);
                }
            }
        };
    }

    private void connectDevice() {
        HashMap usbDevices = usbManager.getDeviceList();
        if ( !usbDevices.isEmpty() ) {
            boolean keep = true;
            for (Object entry : usbDevices.entrySet()) {
                device = (UsbDevice)((Map.Entry)entry).getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2A03) {
                    PendingIntent pi = PendingIntent.getBroadcast(DrumActivity.this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    registerReceiver(broadcastReceiver, filter);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
//                            setUiEnabled(true); //Enable Buttons in UI
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback); //

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                connectDevice();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                serialPort.close();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_snare:
                soundPool.play(soundId[0], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "SNARE!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_crash:
                soundPool.play(soundId[1], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "CRASH!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_hihat:
                soundPool.play(soundId[2], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "HI HAT!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_midtom:
                soundPool.play(soundId[3], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "MIDDLE TOM!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_floortom:
                soundPool.play(soundId[4], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "FLOOR TOM!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_bass:
                soundPool.play(soundId[5], 1.0F, 1.0F, 1, 0, 1.0F);
                Toast.makeText(this, "BASS!", Toast.LENGTH_SHORT).show();
                connectDevice();
                break;
            case R.id.button_hand:
                if (rightHanded) {
                    button_hand.setText("To right-handed mode");
                    rightHanded = false;
                } else {
                    button_hand.setText("To left-handed mode");
                    rightHanded = true;
                }
            default:
                break;
        }
    }

    private void initSensor() {
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
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
        currentX += event.values[0];
        currentY += event.values[1];
        currentZ += event.values[2];

        if (rightHanded) {
            if (event.values[2] > 5) {
                swing = true;
            } else if (swing && prevZ > event.values[2]) {
                swing = false;
                if (!first) {
                    state = newStateR();
                } else {
                    first = false;
                }
                soundPool.play(soundId[state], 1.0F, 1.0F, 1, 0, 1.0F);
                lastX = currentX;
                lastY = currentY;
                lastZ = currentZ;
            }
            prevZ = event.values[2];
        } else {
            if (event.values[2] < -5) {
                swing = true;
            } else if (swing && prevZ < event.values[2]) {
                swing = false;
                if (!first) {
                    state = newStateL();
                } else {
                    first = false;
                }
                soundPool.play(soundId[state], 1.0F, 1.0F, 1, 0, 1.0F);
                lastX = currentX;
                lastY = currentY;
                lastZ = currentZ;
            }
            prevZ = event.values[2];
        }
    }

    private int newStateR() {
        int hMargin = 80;
        switch (state) {
            case 4:
                if (currentZ - lastZ < -80) {
                    if (currentY - lastY > hMargin * 2) return 2;
                    return 1;
                }
                if (currentY - lastY > hMargin * 2) return 0;
                if (currentY - lastY > hMargin) return 3;
                return 4;

            case 2:
                if (currentZ - lastZ > 80) {
                    if (currentY - lastY < -(hMargin * 2)) return 4;
                    if (currentY - lastY < -hMargin) return 3;
                    return 0;
                }
                if (currentY - lastY < -(hMargin * 2)) return 1;
                return 2;

            case 1:
                if (currentZ - lastZ > 80) {
                    if (currentY - lastY > hMargin * 2) return 0;
                    if (currentY - lastY > hMargin) return 3;
                    return 4;
                }
                if (currentY - lastY > hMargin * 2) return 2;
                return 1;

            case 3:
                if (currentZ - lastZ < -80) {
                    if (currentY - lastY < 0) return 1;
                    return 2;
                }
                if (currentY - lastY < -hMargin) return 4;
                if (currentY - lastY > hMargin) return 0;
                return 3;

            case 0:
                if (currentZ - lastZ < -80) {
                    if (currentY - lastY < -(hMargin * 2)) return 1;
                    return 2;
                }
                if (currentY - lastY < -(hMargin * 2)) return 4;
                if (currentY - lastY < -hMargin) return 3;
                return 0;

            default:
                return state;
        }
    }

    private int newStateL() {
        int hMargin = 80;
        switch (state) {
            case 0:
                if (currentZ - lastZ > 80) {
                    if (currentY - lastY < -(hMargin * 2)) return 1;
                    return 2;
                }
                if (currentY - lastY < -(hMargin * 2)) return 4;
                if (currentY - lastY < -hMargin) return 3;
                return 0;

            case 1:
                if (currentZ - lastZ < -80) {
                    if (currentY - lastY > hMargin * 2) return 0;
                    if (currentY - lastY > hMargin) return 3;
                    return 4;
                }
                if (currentY - lastY > hMargin * 2) return 2;
                return 1;

            case 2:
                if (currentZ - lastZ < -80) {
                    if (currentY - lastY < -(hMargin * 2)) return 4;
                    if (currentY - lastY < -hMargin) return 3;
                    return 0;
                }
                if (currentY - lastY < -(hMargin * 2)) return 1;
                return 2;

            case 3:
                if (currentZ - lastZ > 80) {
                    if (currentY - lastY > 0) return 2;
                    return 1;
                }
                if (currentY - lastY > hMargin) return 0;
                if (currentY - lastY < -hMargin) return 4;
                return 3;

            case 4:
                if (currentZ - lastZ > 80) {
                    if (currentY - lastY > hMargin * 2) return 2;
                    return 1;
                }
                if (currentY - lastY > hMargin * 2) return 0;
                if (currentY - lastY > hMargin) return 3;
                return 4;

            default:
                return state;
        }
    }

}
