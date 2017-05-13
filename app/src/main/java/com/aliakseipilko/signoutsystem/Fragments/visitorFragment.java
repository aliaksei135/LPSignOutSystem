/*
 * com.aliakseipilko.signoutsystem.Fragments.visitorFragment was created by Aliaksei Pilko as part of SignOutSystem
 * Copyright (c) Aliaksei Pilko 2017.  All Rights Reserved.
 *
 * Last modified 13/05/17 14:05
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

import com.aliakseipilko.signoutsystem.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class visitorFragment extends Fragment {

    @BindView(R.id.visitorSignInButton)
    Button signInButton;
    @BindView(R.id.visitorSignOutButton)
    Button signOutButton;
    private Unbinder unbinder;
    private OnFragmentInteractionListener mListener;


    public visitorFragment() {
        // Required empty public constructor
    }

    public static visitorFragment newInstance(String param1) {
        visitorFragment frag = new visitorFragment();
        Bundle bundle = new Bundle();
        if (param1.equals("Signed In")) {
            bundle.putBoolean("isSignedIn", true);
        } else {
            bundle.putBoolean("isSignedIn", false);
        }
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_visitor, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isSignedIn = getArguments().getBoolean("isSignedIn");
        if (isSignedIn) {
            signInButton.setBackgroundColor(Color.RED);
            signInButton.setOnClickListener(null);

            signOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonPressed("SIGN_IN");
                }
            });
        } else {
            signOutButton.setBackgroundColor(Color.RED);
            signOutButton.setOnClickListener(null);

            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonPressed("SIGN_OUT");
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

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity. <p> See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating
     * with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String type);
    }
}
