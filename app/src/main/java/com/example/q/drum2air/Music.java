package com.example.q.drum2air;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Music {

    MediaPlayer mediaPlayer = new MediaPlayer();
    String[] AudioPath;
    Context context;

    public Music(Context context) {
        this.context = context;
    }

    public String[] getAudioList() {
        final Cursor mCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA }, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        int count = mCursor.getCount();

        String[] songs = new String[count];
        AudioPath = new String[count];
        int i = 0;
        if (mCursor.moveToFirst()) {
            do {
                songs[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                AudioPath[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                i++;
            } while (mCursor.moveToNext());
        }

        mCursor.close();

        return songs;
    }

    public void playSong(String path) throws IllegalArgumentException, IllegalStateException, IOException {
        Log.d("ringtone", "playSong :: " + path);

        mediaPlayer.reset();
        mediaPlayer.setDataSource(path);
//        mMediaPlayer.setLooping(true);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }
}
