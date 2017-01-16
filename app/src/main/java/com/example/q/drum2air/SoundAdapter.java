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
    ArrayList<String> soundSet;
    ArrayList<Integer> soundId;
    ArrayList<String> soundName;

    public SoundAdapter(SoundPool soundPool) {
        this.soundPool = soundPool;
        soundSet = new ArrayList<>();
        soundId = new ArrayList<>();
        soundName = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return soundSet.size();
    }

    @Override
    public Object getItem(int position) {
        return new Pair<>(soundId.get(position), soundName.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Integer[] getSoundSet(int position) {
        return soundId.subList(position*6, position*6 + 6).toArray(new Integer[0]);
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
        soundItemName.setText(soundSet.get(pos));

        return convertView;
    }

    public void addSound(int id, String name) {
        soundId.add(id);
        soundName.add(name);
    }

    public void addSoundSet(String setName) {
        soundSet.add(setName);
    }

    public void clearAdapter() {
        soundSet.clear();
        soundId.clear();
        soundName.clear();
    }
}
