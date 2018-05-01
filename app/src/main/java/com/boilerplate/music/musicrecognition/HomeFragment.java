package com.boilerplate.music.musicrecognition;


import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    ImageView record_audio;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        record_audio = view.findViewById(R.id.record_audio);
        record_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent recordIntent = new Intent(getContext(), Recognize.class);

                startActivity(recordIntent);

            }
        });


        return view;
    }

}
