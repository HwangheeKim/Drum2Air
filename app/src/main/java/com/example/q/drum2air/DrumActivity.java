package com.example.q.drum2air;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DrumActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, SensorEventListener{

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    Music music;
    String[] MusicList;
    boolean musicPlaying = false;

    Button button_hihat, button_snare, button_midtom, button_crash, button_floortom, button_bass, button_hand, button_music;
    ImageView image_logo;
    boolean logo = true;

    SensorManager sensorManager;
    Sensor sensorGyroscope;

    SoundPool soundPool;
    SoundAdapter soundBank;
    Integer[] soundId = new Integer[6];

    double currentX = 0, currentY = 0, currentZ = 0, lastX = 0, lastY = 0, lastZ = 0, prevZ = 0;
    int state = 3;
    boolean first = true;
    boolean swing = false;
    boolean rightHanded = true;
    float strength = 0;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbManager usbManager;
    UsbDeviceConnection connection;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbSerialInterface.UsbReadCallback mCallback;

    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drum);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        button_hihat = (Button) findViewById(R.id.button_hihat);
        button_snare = (Button) findViewById(R.id.button_snare);
        button_midtom = (Button) findViewById(R.id.button_midtom);
        button_crash = (Button) findViewById(R.id.button_crash);
        button_floortom = (Button) findViewById(R.id.button_floortom);
        button_bass = (Button) findViewById(R.id.button_bass);
        button_hand = (Button) findViewById(R.id.button_hand);
        button_music = (Button) findViewById(R.id.button_music);

        button_hihat.setOnClickListener(this);
        button_snare.setOnClickListener(this);
        button_midtom.setOnClickListener(this);
        button_crash.setOnClickListener(this);
        button_floortom.setOnClickListener(this);
        button_bass.setOnClickListener(this);
        button_hand.setOnClickListener(this);
        button_music.setOnClickListener(this);

        button_hihat.setOnLongClickListener(this);
        button_snare.setOnLongClickListener(this);
        button_midtom.setOnLongClickListener(this);
        button_crash.setOnLongClickListener(this);
        button_floortom.setOnLongClickListener(this);
        button_bass.setOnLongClickListener(this);

        // sensor
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // sound effect
        soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        soundBank = new SoundAdapter(soundPool);
        initSoundBank();

        // Arduino
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mCallback = new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] bytes) {
                if (bytes != null && bytes.length > 0 && bytes[0] == 'T') {
                    soundPool.play(soundId[5], 1.0F, 1.0F, 1, 0, 1.0F);
                }
            }
        };

        // Music
        music = new Music(this);
        MusicList = music.getAudioList();

        // Vibrator
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        // Easter egg
        image_logo = (ImageView) findViewById(R.id.image_logo);
        image_logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (logo) {
                    image_logo.setImageResource(R.drawable.easter);
                    logo = false;
                    Toast.makeText(getApplicationContext(), "Have a good time with Drum2Air!", Toast.LENGTH_SHORT).show();
                } else {
                    image_logo.setImageResource(R.drawable.logo);
                    logo = true;
                    Toast.makeText(getApplicationContext(), "Do you hate us? :(", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void initSoundBank() {

        soundBank.addSoundSet("Classic");
        soundBank.addSound(soundPool.load(this, R.raw.snare, 1), "Snare");
        soundBank.addSound(soundPool.load(this, R.raw.crash, 1), "Crash");
        soundBank.addSound(soundPool.load(this, R.raw.hihat, 1), "Hihat");
        soundBank.addSound(soundPool.load(this, R.raw.midtom, 1), "Midtom");
        soundBank.addSound(soundPool.load(this, R.raw.floortom, 1), "Floortom");
        soundBank.addSound(soundPool.load(this, R.raw.bass, 1), "Bass");

        soundBank.addSoundSet("Synth");
        soundBank.addSound(soundPool.load(this, R.raw.syn_1, 1), "Syn 1");
        soundBank.addSound(soundPool.load(this, R.raw.syn_2, 1), "Syn 2");
        soundBank.addSound(soundPool.load(this, R.raw.syn_3, 1), "Syn 3");
        soundBank.addSound(soundPool.load(this, R.raw.syn_4, 1), "Syn 4");
        soundBank.addSound(soundPool.load(this, R.raw.syn_5, 1), "Syn 5");
        soundBank.addSound(soundPool.load(this, R.raw.syn_bass, 1), "Syn Bass");
        // TODO : Add more sounds!

        soundId = soundBank.getSoundSet(0);
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
                break;
            case R.id.button_music:
                if (!musicPlaying) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("Which music do you wanna play?");
                    dialog.setItems(MusicList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                music.playSong(music.AudioPath[which]);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            button_music.setText("Stop music");
                            musicPlaying = true;
                        }
                    });
                    dialog.show();
                } else {
                    music.mediaPlayer.stop();
                    button_music.setText("Choose music");
                    musicPlaying = false;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        FragmentManager fm = getFragmentManager();
        SoundChooserDialogFragment dialogFragment = new SoundChooserDialogFragment();
        Bundle args = new Bundle();

        switch(v.getId()) {
            // TODO : Implement here!
            case R.id.button_snare:
                args.putInt("selected", 0);
                break;
            case R.id.button_crash:
                args.putInt("selected", 1);
                break;
            case R.id.button_hihat:
                args.putInt("selected", 2);
                break;
            case R.id.button_midtom:
                args.putInt("selected", 3);
                break;
            case R.id.button_floortom:
                args.putInt("selected", 4);
                break;
            case R.id.button_bass:
                args.putInt("selected", 5);
                break;
            default:
                break;
        }
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "sound_chooser");
        return true;
    }

    public void changeSound(int selected, int to) {
        soundId = soundBank.getSoundSet(to);
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

                Log.d("prevZ&currZ", "prevZ : " + prevZ + ", currZ : " + event.values[2]);
                if (prevZ - event.values[2] > 2.0) {
                    strength = 1.0f;
                } else if (prevZ - event.values[2] > 1.0) {
                    strength = 0.7f;
                } else {
                    strength = 0.4f;
                }

                Log.d("STATE&STR", "STATE : " + state + ", STRENGTH : " + strength);
                soundPool.play(soundId[state], strength, strength, 1, 0, 1.0F);

                if (strength == 1.0f && state == 1) {
                    v.vibrate(500);
                }

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

                Log.d("prevZ&currZ", "prevZ : " + prevZ + ", currZ : " + event.values[2]);
                if (prevZ - event.values[2] < -2.0) {
                    strength = 1.0f;
                } else if (prevZ - event.values[2] < -1.0) {
                    strength = 0.7f;
                } else {
                    strength = 0.4f;
                }

                if (strength == 1.0f && state == 1) {
                    v.vibrate(500);
                }
                Log.d("STATE&STR", "STATE : " + state + ", STRENGTH : " + strength);
                soundPool.play(soundId[state], strength, strength, 1, 0, 1.0F);
                lastX = currentX;
                lastY = currentY;
                lastZ = currentZ;
            }
            prevZ = event.values[2];
        }
    }

    private int newStateR() {
        int hMargin = 100;
        int vMargin = 100;
        switch (state) {
            case 4:
                if (currentZ - lastZ < -vMargin) {
                    if (currentY - lastY > hMargin * 2) return 2;
                    return 1;
                }
                if (currentY - lastY > hMargin * 2) return 0;
                if (currentY - lastY > hMargin) return 3;
                return 4;

            case 2:
                if (currentZ - lastZ > vMargin) {
                    if (currentY - lastY < -(hMargin * 2)) return 4;
                    if (currentY - lastY < -hMargin) return 3;
                    return 0;
                }
                if (currentY - lastY < -(hMargin * 2)) return 1;
                return 2;

            case 1:
                if (currentZ - lastZ > vMargin) {
                    if (currentY - lastY > hMargin * 2) return 0;
                    if (currentY - lastY > hMargin) return 3;
                    return 4;
                }
                if (currentY - lastY > hMargin * 2) return 2;
                return 1;

            case 3:
                if (currentZ - lastZ < -vMargin) {
                    if (currentY - lastY < 0) return 1;
                    return 2;
                }
                if (currentY - lastY < -hMargin) return 4;
                if (currentY - lastY > hMargin) return 0;
                return 3;

            case 0:
                if (currentZ - lastZ < -vMargin) {
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
        int hMargin = 100;
        int vMargin = 100;
        switch (state) {
            case 0:
                if (currentZ - lastZ > vMargin) {
                    if (currentY - lastY < -(hMargin * 2)) return 1;
                    return 2;
                }
                if (currentY - lastY < -(hMargin * 2)) return 4;
                if (currentY - lastY < -hMargin) return 3;
                return 0;

            case 1:
                if (currentZ - lastZ < -vMargin) {
                    if (currentY - lastY > hMargin * 2) return 0;
                    if (currentY - lastY > hMargin) return 3;
                    return 4;
                }
                if (currentY - lastY > hMargin * 2) return 2;
                return 1;

            case 2:
                if (currentZ - lastZ < -vMargin) {
                    if (currentY - lastY < -(hMargin * 2)) return 4;
                    if (currentY - lastY < -hMargin) return 3;
                    return 0;
                }
                if (currentY - lastY < -(hMargin * 2)) return 1;
                return 2;

            case 3:
                if (currentZ - lastZ > vMargin) {
                    if (currentY - lastY > 0) return 2;
                    return 1;
                }
                if (currentY - lastY > hMargin) return 0;
                if (currentY - lastY < -hMargin) return 4;
                return 3;

            case 4:
                if (currentZ - lastZ > vMargin) {
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
