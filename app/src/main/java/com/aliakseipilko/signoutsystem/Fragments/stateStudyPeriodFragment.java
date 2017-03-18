/*
 * com.aliakseipilko.signoutsystem.Fragments.stateStudyPeriodFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 18/03/17 21:33
 */

package com.aliakseipilko.signoutsystem.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aliakseipilko.signoutsystem.R;
import com.sevenheaven.segmentcontrol.SegmentControl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link stateStudyPeriodFragment.OnFragmentInteractionListener} interface to handle interaction
 * events. Use the {@link stateStudyPeriodFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class stateStudyPeriodFragment extends Fragment {

    @BindView(R.id.house_button4)
    SegmentControl houseButton;
    @BindView(R.id.going_to_green_button4)
    Button atGreenButton;
    @BindView(R.id.going_home_button4)
    Button signOutButton;
    @BindView(R.id.sign_in_button4)
    Button signInButton;
    @BindView(R.id.cancel_button4)
    Button cancelButton;
    Unbinder unbinder;
    private OnFragmentInteractionListener mListener;

    public stateStudyPeriodFragment() {
        // Required empty public constructor
    }

    public static stateStudyPeriodFragment newInstance() {

        return new stateStudyPeriodFragment();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

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

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("SIGN_OUT");
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("SIGN_IN");
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("CANCEL");
            }
        });

        int year = getArguments().getInt("year");

        if (year > 11) {
            atGreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonPressed("GREEN");
                }
            });
        } else {
            atGreenButton.setBackgroundColor(Color.RED);
            atGreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Not Allowed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_state_study_period, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
