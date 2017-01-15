package com.example.q.drum2air;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by q on 2017-01-15.
 */

public class SoundAdapter extends BaseAdapter {
    SoundPool soundPool;
    ArrayList<Integer> soundId;
    ArrayList<String> soundName;

    public SoundAdapter(SoundPool soundPool) {
        this.soundPool = soundPool;
        soundId = new ArrayList<>();
        soundName = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return soundId.size();
    }

    @Override
    public Object getItem(int position) {
        return new Pair<>(soundId.get(position), soundName.get(position));
    }

    public int getSoundId(int position) {
        return soundId.get(position);
    }

    public String getName(int position) {
        return soundName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Click 'sound icon' to play sample sound
        final int pos = position;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dialog_sound_item, parent, false);
        }

        TextView soundItemName = (TextView) convertView.findViewById(R.id.sound_item_name);
        ImageView soundItemPlay = (ImageView) convertView.findViewById(R.id.sound_item_play);

        soundItemName.setText(soundName.get(pos));

        soundItemPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundId.get(pos), 1.0F, 1.0F, 1, 0, 1.0F);
            }
        });

        return convertView;
    }

    public void addSound(int id, String name) {
        soundId.add(id);
        soundName.add(name);
    }

    public void clearSound() {
        soundId.clear();
        soundName.clear();
    }
}
