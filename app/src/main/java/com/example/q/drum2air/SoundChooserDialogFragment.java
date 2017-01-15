package com.example.q.drum2air;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by q on 2017-01-15.
 */

public class SoundChooserDialogFragment extends DialogFragment {
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sound_chooser, container, false);
        final int selected = getArguments().getInt("selected");

        listView = (ListView)view.findViewById(R.id.sound_chooser_list);
        listView.setAdapter(((DrumActivity)getActivity()).soundBank);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((DrumActivity)getActivity()).changeSound(selected, position);
                dismiss();
            }
        });

        return view;
    }
}
