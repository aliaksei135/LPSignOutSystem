/*
 * com.aliakseipilko.signoutsystem.Fragments.stateVisitHouseFragment was created by Aliaksei Pilko as part of SignOutSystem
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class stateVisitHouseFragment extends Fragment {

    @BindView(R.id.study_period_button5)
    Button studyPeriodButton;
    @BindView(R.id.going_home_button5)
    Button signOutButton;
    @BindView(R.id.sign_in_button5)
    Button signInButton;
    @BindView(R.id.cancel_button5)
    Button cancelButton;
    @BindView(R.id.going_to_green_button5)
    Button atGreenButton;
    Unbinder unbinder;
    private OnFragmentInteractionListener mListener;

    public stateVisitHouseFragment() {
        // Required empty public constructor
    }

    public static stateVisitHouseFragment newInstance(String param1) {

        stateVisitHouseFragment fragment = new stateVisitHouseFragment();
        Bundle args = new Bundle();
        args.putString("HOUSE", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_state_visit_house, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        studyPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed("STUDY_PERIOD");
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

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String type);
    }
}
