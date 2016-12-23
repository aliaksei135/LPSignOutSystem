/*
 * com.aliakseipilko.signoutsystem.Fragments.stateAtGreenFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2016.  All Rights Reserved.
 *
 * Last modified 23/12/16 13:12
 */

package com.aliakseipilko.signoutsystem.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aliakseipilko.signoutsystem.R;
import com.sevenheaven.segmentcontrol.SegmentControl;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link stateAtGreenFragment.OnFragmentInteractionListener} interface to handle interaction
 * events. Use the {@link stateAtGreenFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class stateAtGreenFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public stateAtGreenFragment() {
        // Required empty public constructor
    }


    public static stateAtGreenFragment newInstance() {

        return new stateAtGreenFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_state_at_green, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        SegmentControl houseButton = (SegmentControl) getView().findViewById(R.id.house_button3);
        houseButton.setOnSegmentControlClickListener(new SegmentControl.OnSegmentControlClickListener() {
            @Override
            public void onSegmentControlClick(int index) {
                switch (index) {
                    case 0:
                        onButtonPressed("VISIT_HOUSE_FIELD");
                        break;
                    case 1:
                        onButtonPressed("VISIT_HOUSE_FRYER");
                    case 2:
                        onButtonPressed("VISIT_HOUSE_GROVE");
                        break;
                    case 3:
                        onButtonPressed("VISIT_HOUSE_RECKITT");

                }
            }
        });
        Button studyPeriodButton = (Button) getView().findViewById(R.id.study_period_button3);
        studyPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("STUDY_PERIOD");
            }
        });
        Button signOutButton = (Button) getView().findViewById(R.id.going_home_button3);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("SIGN_OUT");
            }
        });
        Button signInButton = (Button) getView().findViewById(R.id.sign_in_button3);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("SIGN_IN");
            }
        });
        Button cancelButton = (Button) getView().findViewById(R.id.cancel_button3);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("CANCEL");
            }
        });
    }

    private void onButtonPressed(String type) {

        if (mListener != null) {
            mListener.onFragmentInteraction(type);
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity. <p/> See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating
     * with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String type);
    }
}
